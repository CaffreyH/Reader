package readApp.activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import readApp.book.Book;
import readApp.sqlite.DatabaseHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MainActivity extends Activity {
	private static final String SERVER_IP = "220.167.45.249";		//������IP��ַ
	private static final int SERVER_PORT = 8000;		//�˿�
	private static final int TIMEOUT = 5000;		//��ʱʱ��
	private static final int START_IMAGE_TIME = 2000;		//��ʼͼƬ����ʱ��
	private static final int START_IMAGE_REMOVE_TIME = 500;		//��ʼͼƬ��ʧʱ��
	
	private static final int MESSAGE_CLOSE_START_IMAGE = 0;		//�رտ�ʼͼƬ�ź�
	
	public static final int SIGN_BREAK_CONNECT = -1;		//�Ͽ����ӱ�־
	public static final int SIGN_SEND_BAGRRAGE = 0;		//���͵�Ļ��־
	public static final int SIGN_ASK_PAGE_BAGRRAGES = 1;		//����һҳ��Ļ��־
	public static final int SIGN_DOWNLOAD_CHAPTER = 2;		//�����½ڱ�־
	public static final int SIGN_ASK_CELLING_BOOK = 3;		//��ȡ������
	
	public static final float LINE_SPACING = 1f/3;		//�о�
	public static final float PARA_SPACING = 1f/3;		//�ξ�
	
	public static boolean isLogin = false;
	public static SQLiteDatabase database;		//���ݿ����
	public static DataInputStream dis;		//������
	public static DataOutputStream dos;		//�����
	private static boolean isConnected;		//�ж��Ƿ����Ϸ�����
	public static LinkedList<Book> booksList;		//���ݿ����е���
	private Socket socket;		//�ͻ����׽���
	
	private static Runnable connectServerTask;
	private Runnable showStartImageTask;
	
	private Handler mainHandler;
	
	private FrameLayout mainLayout;
	private ImageView startImage;
	private ImageView[] firstUseImages;
	private Button loginBtn;
	private Button createBtn;
	private Button lookBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		init();
	}
	
	@SuppressLint("NewApi")
	private void init() {
		booksList = new LinkedList<Book>();
		isConnected = false;
		
		initView();
		openSQLite();
		initHandler();
		initTask();
		new Thread(showStartImageTask).start();
		connectServer();
	}
	
	private void openSQLite() {
		DatabaseHelper dbHelper = new DatabaseHelper(this, "readDB.db3", null, 1);
		database = dbHelper.getReadableDatabase();
		Cursor cursor = MainActivity.database.rawQuery("select * from "
				+ DatabaseHelper.TABLE_APP, null);
		cursor.moveToFirst();
		int isFirstUse = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.APP_FIRST_USE));
		if(isFirstUse == 1){
			firstUse();
		}
		updateSQLBooks();
	}
	
	private void initView() {
		mainLayout = (FrameLayout) findViewById(R.id.main_layout);
		startImage = (ImageView) findViewById(R.id.start_image);
		loginBtn = (Button) findViewById(R.id.login_btn);
		createBtn = (Button) findViewById(R.id.create_btn);
		lookBtn = (Button) findViewById(R.id.look_btn);
		
		initViewEvent();
	}
	
	private void initViewEvent(){
		
		loginBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		});
		
		createBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, CreateActivity.class);
				startActivity(intent);
			}
		});
		
		lookBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ShopActivity.class);
				startActivity(intent);
			}
		});
	}
	
	@SuppressLint("HandlerLeak")
	private void initHandler(){
		mainHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what){
				case MESSAGE_CLOSE_START_IMAGE:
					Animation anim = new AlphaAnimation(1, 0);
					anim.setDuration(START_IMAGE_REMOVE_TIME);
					startImage.setAnimation(anim);
					startImage.startAnimation(anim);
					anim.setAnimationListener(new AnimationListener(){

						@Override
						public void onAnimationStart(Animation animation) {
							
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							mainLayout.removeView(startImage);
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							
						}
						
					});
					break;
				}
			}
			
		};
	}
	
	private void initTask() {
		connectServerTask = new Runnable(){

			@Override
			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), TIMEOUT);
					dos = new DataOutputStream(socket.getOutputStream());
					dis = new DataInputStream(socket.getInputStream());
					isConnected = true;
				} catch (Exception e) {
				}
			}
			
		};
		
		showStartImageTask = new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(START_IMAGE_TIME);
					mainHandler.sendEmptyMessage(MESSAGE_CLOSE_START_IMAGE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		};
	}
	
	private void updateSQLBooks(){
		Cursor cursor = database.rawQuery("select * from "
				+ DatabaseHelper.TABLE_BOOK, 
				null);
		while(cursor.moveToNext()){
			int bookId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.BOOK_ID));
			String bookName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.BOOK_NAME));
			String bookWriter = cursor.getString(cursor.getColumnIndex(DatabaseHelper.BOOK_WRITER));
			String bookState = cursor.getString(cursor.getColumnIndex(DatabaseHelper.BOOK_STATE));
			String bookIntroduction = cursor.getString(cursor.getColumnIndex(DatabaseHelper.BOOK_INTRODUCTION));
			int totalChapterNum = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.BOOK_TOTAL_CHAPTER_NUM));
			int lastChapterNum = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.BOOK_LAST_CHAPTER_NUM));
			int lastPageNum = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.BOOK_LAST_PAGE_NUM));
			byte[] image = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.BOOK_IMAGE));
			
			Book book = new Book(bookId, bookName, bookWriter, bookState, bookIntroduction, totalChapterNum, lastChapterNum, lastPageNum);
			book.setBookImage(this, image);
			booksList.add(book);
			Log.i("message", "" + booksList.size());
		}
	}
	
	public static void connectServer(){
		if(!isConnected){
			new Thread(connectServerTask).start();
		}
	}
	
	public static void destroyConnect(){
		try {
			dos.writeInt(SIGN_BREAK_CONNECT);
			isConnected = false;
		} catch (Exception e) {
		}
	}
	
	@SuppressLint("NewApi")
	private void firstUse() {
		final int MAX_IMAGES_NUM = 3;
		firstUseImages = new ImageView[MAX_IMAGES_NUM];
		firstUseImages[0] = new ImageView(this);
		firstUseImages[1] = new ImageView(this);
		firstUseImages[2] = new ImageView(this);
		firstUseImages[0].setImageDrawable(getDrawable(R.drawable.first_use0));
		firstUseImages[1].setImageDrawable(getDrawable(R.drawable.first_use1));
		firstUseImages[2].setImageDrawable(getDrawable(R.drawable.first_use2)); 
		for(int i = 0; i < MAX_IMAGES_NUM; i++){
			mainLayout.addView(firstUseImages[i]);
			firstUseImages[i].setScaleType(ScaleType.FIT_XY);
			firstUseImages[i].setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					ImageView iv = (ImageView) mainLayout.getChildAt(mainLayout.getChildCount()-1);
					Animation anim = new AlphaAnimation(1, 0);
					anim.setDuration(START_IMAGE_REMOVE_TIME);
					iv.setAnimation(anim);
					iv.startAnimation(anim);
					iv.setEnabled(false);
					anim.setAnimationListener(new AnimationListener(){

						@Override
						public void onAnimationStart(Animation animation) {
							
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							mainLayout.removeViewAt(mainLayout.getChildCount()-1);
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							
						}
						
					});
				}
				
			});
		}
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.APP_FIRST_USE, 0);
		database.update(DatabaseHelper.TABLE_APP, cv, "", null);
	}
	
	public static boolean isConnected() {
		return isConnected;
	}

	public static void addBook(Book book){
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.BOOK_ID, book.getBookId());
		cv.put(DatabaseHelper.BOOK_NAME, book.getBookName());
		cv.put(DatabaseHelper.BOOK_WRITER, book.getBookWriter());
		cv.put(DatabaseHelper.BOOK_STATE, book.getBookState());
		cv.put(DatabaseHelper.BOOK_INTRODUCTION, book.getBookIntro());
		cv.put(DatabaseHelper.BOOK_TOTAL_CHAPTER_NUM, 0);
		cv.put(DatabaseHelper.BOOK_LAST_CHAPTER_NUM, 0);
		cv.put(DatabaseHelper.BOOK_LAST_PAGE_NUM, 0);
		cv.put(DatabaseHelper.BOOK_IMAGE, book.getImageBytes());
		database.insert(DatabaseHelper.TABLE_BOOK, null, cv);
		booksList.add(book);
	}
	
	public static void deleteBook(Book book){
		int bookId = book.getBookId();
		database.delete(DatabaseHelper.TABLE_BOOK,"id=?",
				new String[]{"" + bookId});
		database.delete(DatabaseHelper.TABLE_CHAPTER,"id=?",
				new String[]{"" + bookId});
		database.delete(DatabaseHelper.TABLE_NOTE,"id=?",
				new String[]{"" + bookId});
	}

	@Override
	protected void onDestroy() {
		database.close();
		destroyConnect();
		super.onDestroy();
	}
}
