package readApp.activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.LinkedList;

import readApp.book.Book;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShopActivity extends Activity implements OnGestureListener {
	private static final int BUFFER_SIZE = 5;		//缓存区大小
	private LinkedList<Book> booksBuffer = new LinkedList<Book>();		//缓存区
	
	private AskBookThread askBookThread;
	private boolean isAskBookOpen;
	private ImageView imageView;
	private TextView nameView;
	private TextView writerView;
	private TextView stateView;
	private TextView introView;
	private Button startReadBtn;
	private Button backBtn;
	private Button shelfBtn;
	
	private Book currentBook;
	
	private GestureDetector detector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop);
		
		init();
		try{
			while(booksBuffer.isEmpty()) {
				Thread.sleep(16);
			}
		} catch (Exception e) {
		}
		nextBook();
	}
	
	private void init(){
		initView();
		openAskBookThread();
		detector = new GestureDetector(this, this);
	}
	
	private void initView(){
		imageView = (ImageView) findViewById(R.id.image);
		nameView = (TextView) findViewById(R.id.name);
		writerView = (TextView) findViewById(R.id.writer);
		stateView = (TextView) findViewById(R.id.state);
		introView = (TextView) findViewById(R.id.intro);
		startReadBtn = (Button) findViewById(R.id.start_read);
		backBtn = (Button) findViewById(R.id.back_btn);
		shelfBtn = (Button) findViewById(R.id.shelf_btn);
		initViewEvent();
	}
	
	private void initViewEvent(){
		startReadBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				startRead();
			}
			
		});
		
		backBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
		shelfBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!MainActivity.isLogin) {
					Toast.makeText(getApplicationContext(), "请先登录", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent();
				intent.setClass(ShopActivity.this, ShelfActivity.class);
				startActivity(intent);
				finish();
			}
			
		});
	}
	
	private void nextBook(){
		currentBook = removeBookFromBuffer();
		imageView.setImageDrawable(currentBook.getBookImage());
		nameView.setText(currentBook.getBookName());
		writerView.setText("作者：" + currentBook.getBookWriter());
		stateView.setText("状态：" + currentBook.getBookState());
		introView.setText(currentBook.getBookIntro());
	}
	
	private void openAskBookThread(){
		isAskBookOpen = true;
		askBookThread = new AskBookThread();
		askBookThread.start();
	}
	
	private class AskBookThread extends Thread {
		public void run(){
			while(isAskBookOpen){
				synchronized (booksBuffer) {
					while(booksBuffer.size() >= BUFFER_SIZE){
						try{
							booksBuffer.wait();
						} catch (Exception e) {
						}
					}
				}
				new AskBookTask().run();
			}
		}
	}
	
	private class AskBookTask implements Runnable {
		public void run (){
			synchronized (MainActivity.dos) {
				DataOutputStream dos = MainActivity.dos;
				DataInputStream dis = MainActivity.dis;
				try {
					dos.writeInt(MainActivity.SIGN_ASK_CELLING_BOOK);
					int bookId = dis.readInt();
					String bookName = dis.readUTF();
					String bookWriter = dis.readUTF();
					String bookState = dis.readUTF();
					String bookIntroduction = dis.readUTF();
					int length = dis.readInt();
					byte[] image = new byte[length];
					for(int i = 0; i < length; i += dis.read(image, i, length - i));
					Book book = new Book(bookId, bookName, bookWriter, bookState, bookIntroduction, 0, 0, 0);
					book.setBookImage(ShopActivity.this, image);
					addBookToBuffer(book);
				} catch (Exception e) {
				}
			}
		}
	}
	
	private void startRead(){
		Intent intent = new Intent();
		intent.putExtra("bookId", currentBook.getBookId());
		intent.setClass(ShopActivity.this, ReadActivity.class);
		startActivity(intent);
		if(!isOwned(currentBook)){
			MainActivity.addBook(currentBook);
		}
	}
	
	private void addBookToBuffer(Book book){
		synchronized (booksBuffer) {
			booksBuffer.addLast(book);
			booksBuffer.notify();
		}
	}
	
	private boolean isOwned(Book book){
		LinkedList<Book> booksList = MainActivity.booksList;
		for(int i = 0; i < booksList.size(); i++){
			if(book.getBookId() == booksList.get(i).getBookId()){
				return true;
			}
		}
		return false;
	}
	
	private Book removeBookFromBuffer(){
		Book book = null;
		synchronized (booksBuffer) {
			if(!booksBuffer.isEmpty()){
				book = booksBuffer.removeFirst();
				booksBuffer.notify();
			}
		}
		return book;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return detector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(velocityX < 0){
			nextBook();
		}
		return true;
	}

	private void closeAskBookThread(){
		isAskBookOpen = false;
		try {
			askBookThread.interrupt();
		}  catch (Exception e) {
			
		}
	}

	@Override
	protected void onDestroy() {
		closeAskBookThread();
		super.onDestroy();
	}
}
