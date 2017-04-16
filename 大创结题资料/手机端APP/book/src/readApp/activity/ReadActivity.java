package readApp.activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.DecimalFormat;
import java.util.LinkedList;

import readApp.book.Book;
import readApp.book.Chapter;
import readApp.book.Line;
import readApp.book.Note;
import readApp.book.Word;
import readApp.sqlite.DatabaseHelper;
import readApp.support.Style;
import readApp.support.Tools;
import readApp.view.BagrrageView;
import readApp.view.BottomMenu;
import readApp.view.ContentView;
import readApp.view.NoteMenu;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

public class ReadActivity extends Activity implements OnGestureListener {
	private final int BAGRRAGE_PATH_HEIGHT = 40;		//弹幕路径高度
	private final int BAGRRAGE_PATH_NUM = 6;		//弹幕路径数量
	private final float INFOSIZE_TO_TEXTSIZE = 0.65f;		//屏幕四角的书籍信息字体相对于文字字体大小的比例
	
	public static float titleTextHeight = 400;		//标题的高度
	public static float titleLineSpacing = 20;		//标题分割线距离标题的高度
	public static float titleLineHeight = titleTextHeight+titleLineSpacing;		//标题分割线的高度
	
	public static final int MSG_UPDATE = 0;		//更新contenrView
	public static final int MSG_SHOW_BAGRRAGE = 1;		//显示一条弹幕
	public static final int MSG_SHOW_TIME = 2;		//提醒阅读时间
	
	public static final int LONG = 10000;
	public static final int SHORT = 1000;
	
	public int time;
	
	public static int chapterBufferSize = 5;
	
	private int bookId;
	private Book book;
	private Style style;
	
	private boolean isBagrrageOpen;
	
	private RelativeLayout topLayout;
	private RelativeLayout readLayout;
	private LinearLayout contentLayout;
	private ContentView contentView;
	private LinearLayout bagrrageLayout;
	private FrameLayout[] bagrragePathViews;
	private TextView bookNameView;
	private TextView chapterNameView;
	private TextClock timeView;
	private TextView processView;
	private LinearLayout bagrrageEditLayout;
	private EditText bagrrageEditText;
	private Button bagrrageBtn;
	
	private LinkedList<String> bagrrages;
	
	private GestureDetector detector;
	
	private boolean isChoosing;
	private boolean isChanging;
	private boolean isChangeStart;
	private boolean isChangeEnd;
	private Word startWord;
	private Word endWord;
	
	private NoteMenu noteMenu;
	private BottomMenu bottomMenu;
	private boolean isNoteMenuOpen;
	private boolean isBottomMenuOpen;
	
	private ActivityHandler handler;
	
	private NetThread netThread = new NetThread();
	private AutoDownloadThread autoThread;
	private ShowBagrrageThread showBagrrageThread;
	private ClockThread clockThread;
	private int autoDownloadTime = SHORT;
	private boolean isAutoDownload = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
		init();
	}
	
	//初始化
	private void init(){
		setScreenStyle(true);
		getDataFromIntent();
		book = mkBook(bookId);
		getAppInfo();
		getFirstChapter();
		initViews();
		initViewsStyle();
		contentView.setBook(book);
		updateBookName();
		createBagrrageView();
		handler = new ActivityHandler();
		time = 0;
		
		contentView.setChapter(mkChapter(book), book.getPageNum());
		
		bagrrages = new LinkedList<String>();
		
		detector = new GestureDetector(this, this);
		isChoosing = false;
		isChanging = false;
		isChangeStart = false;
		isChangeEnd = false;
		startWord = null;
		endWord = null;
		
		netThread.start();
		autoThread = new AutoDownloadThread();
		autoThread.start();
		clockThread = new ClockThread();
		clockThread.start();
	}
	
	//从intent获取数据
	private void getDataFromIntent(){
		Intent intent = getIntent();
		bookId = intent.getIntExtra("bookId", -1);
	}

	private Book mkBook(int bookId){
		Book book = null;
		//获取表book信息
		Cursor cursor = MainActivity.database.rawQuery("select * from "
				+ DatabaseHelper.TABLE_BOOK + " where id=?", 
				new String[]{"" + bookId});
		if(cursor.moveToFirst()) {
			String bookName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.BOOK_NAME));
			int totalChapterNum = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.BOOK_TOTAL_CHAPTER_NUM));
			int currentChapterNum = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.BOOK_LAST_CHAPTER_NUM));
			int currentChapterProcess = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.BOOK_LAST_PAGE_NUM));
			book = new Book(bookId, bookName, totalChapterNum, currentChapterNum, currentChapterProcess);
		}
		return book;
	}
	
	//获取表APP信息
	private void getAppInfo(){
		Cursor cursor = MainActivity.database.rawQuery("select * from "
				+ DatabaseHelper.TABLE_APP, null);
		cursor.moveToFirst();
		int textSizeSP = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.APP_TEXT_SIZE));
		String textTypefacePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.APP_TEXT_TYPEFACE_PATH));
		isBagrrageOpen = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.APP_IS_BAGRRAGE_OPEN)) == 1;
		int colorStyle = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.APP_COLOR_STYLE));
		int backgroundColor = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.APP_BACKGROUND_COLOR));
		int textColor = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.APP_TEXT_COLOR));
		int textSize = Tools.sp2px(this, textSizeSP);
		Typeface typeface = Typeface.createFromAsset(this.getAssets(), textTypefacePath);
		style = new Style(textSize, typeface, textColor, backgroundColor, colorStyle);
		style.setTypefaceStr(textTypefacePath);
	}

	//如果总章节数为零，则下载第一章
	private void getFirstChapter(){
		if(book.getTotalChapterNum() == 0){
			new Thread(){
				public void run(){
					synchronized (MainActivity.dos) {
						DataOutputStream dos = MainActivity.dos;
						DataInputStream dis = MainActivity.dis;
						try {
							dos.writeInt(MainActivity.SIGN_DOWNLOAD_CHAPTER);
							dos.writeInt(bookId);
							dos.writeInt(0);
							dos.flush();
							String name = dis.readUTF();
							String content = dis.readUTF();
							saveChapter(name, content, 0);
						} catch (Exception e) {
						}
					}
				}
			}.start();
		}
		while(book.getTotalChapterNum() == 0){
			try {
				Thread.sleep(16);
			} catch (Exception e) {
			}
		}
	}

	//初始化控件
	private void initViews(){
		//topLayout
		topLayout = (RelativeLayout) findViewById(R.id.top);
		//readLayout
		readLayout = (RelativeLayout) findViewById(R.id.readLayout);
		//contentLayout
		contentLayout = (LinearLayout) findViewById(R.id.contentLayout);
		//contentView
		contentView = (ContentView) findViewById(R.id.contentView);
		//bookName
		bookNameView = (TextView) findViewById(R.id.bookName);
		//chapterName
		chapterNameView = (TextView) findViewById(R.id.chapterName);
		//time
		timeView = (TextClock) findViewById(R.id.time);
		//process
		processView = (TextView) findViewById(R.id.process);
		//noteMenu
		noteMenu = (NoteMenu) findViewById(R.id.note_menu);
		//bottomMenu
		bottomMenu = (BottomMenu) findViewById(R.id.bottom_menu);
	}
	
	private void initViewsStyle(){
		contentView.setStyle(style);
		bookNameView.setTypeface(style.getTypeface());
		bookNameView.setTextSize(Tools.px2sp(this, style.getTextSize())
				*INFOSIZE_TO_TEXTSIZE);
		chapterNameView.setTypeface(style.getTypeface());
		chapterNameView.setTextSize(Tools.px2sp(this, style.getTextSize())
				*INFOSIZE_TO_TEXTSIZE);
		timeView.setTypeface(style.getTypeface());
		timeView.setTextSize(Tools.px2sp(this, style.getTextSize())
				*INFOSIZE_TO_TEXTSIZE);
		processView.setTypeface(style.getTypeface());
		processView.setTextSize(Tools.px2sp(this, style.getTextSize())
				*INFOSIZE_TO_TEXTSIZE);
	}
	
	//创建弹幕控件
	@SuppressLint("NewApi")
	private void createBagrrageView(){
		//弹幕控件
		bagrrageLayout = new LinearLayout(this);
		bagrrageLayout.setOrientation(LinearLayout.VERTICAL);
		if(isBagrrageOpen) {
			bagrrageLayout.setVisibility(View.VISIBLE);
			showBagrrageThread = new ShowBagrrageThread();
			showBagrrageThread.start();
			bottomMenu.getBagrrageBtn().setText("关闭弹幕");
		} else {
			bagrrageLayout.setVisibility(View.GONE);
			bottomMenu.getBagrrageBtn().setText("打开弹幕");
		}
		contentLayout.addView(bagrrageLayout, 0);
		//弹幕路径
		bagrragePathViews = new FrameLayout[BAGRRAGE_PATH_NUM];
		for(int i = 0; i < BAGRRAGE_PATH_NUM; i++){
			bagrragePathViews[i] = new FrameLayout(this);
			bagrrageLayout.addView(bagrragePathViews[i]);
			//设置弹幕路径高度
			LinearLayout.LayoutParams params = 
					(LinearLayout.LayoutParams) bagrragePathViews[i].getLayoutParams();
			params.height = BAGRRAGE_PATH_HEIGHT;
			bagrragePathViews[i].setLayoutParams(params);
		}
		//创建输入框
		bagrrageEditLayout = new LinearLayout(this);
		bagrrageEditText = new EditText(this);
		bagrrageBtn = new Button(this);
		bagrrageLayout.addView(bagrrageEditLayout);
		bagrrageEditLayout.addView(bagrrageEditText);
		bagrrageEditLayout.addView(bagrrageBtn);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(40, 0, 0, 0);
		
		bagrrageEditText.setLayoutParams(params);
		bagrrageEditText.setHeight(100);
		bagrrageEditText.setWidth(400);
		bagrrageEditText.setBackground(this.getDrawable(R.drawable.text_shape));
		bagrrageEditText.setSingleLine();
		bagrrageEditText.setTypeface(style.getTypeface());
		bagrrageEditText.setLinkTextColor(Color.RED);
		
		bagrrageBtn.setText("吐槽");
		bagrrageBtn.setBackgroundColor(Color.rgb(0, 127, 255));
		bagrrageBtn.setBackground(this.getDrawable(R.drawable.white_btn));
		bagrrageBtn.setLayoutParams(params);
		bagrrageBtn.setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
				sendBagrrage();
				synchronized (bagrrages) {
					bagrrages.addFirst(bagrrageEditText.getText().toString());
				}
				bagrrageEditText.clearFocus();
				bagrrageEditText.setText("");
		        View view = getWindow().peekDecorView();
		        if (view != null) {
		            InputMethodManager inputmanger = 
		            		(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		        }
			}
			
		});
		bagrrageEditText.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				closeBottomMenu();
				closeNoteMenu();
			}
			
		});
	}
	
	private void sendBagrrage(){
		netThread.execute(new SendBagrrageTask());
	}
	
	public void openBagrrage(){
		isBagrrageOpen = true;
		showBagrrageThread = new ShowBagrrageThread();
		showBagrrageThread.start();
		bagrrageLayout.setVisibility(View.VISIBLE);
		handler.sendEmptyMessage(MSG_UPDATE);
		netThread.execute(new AskPageBagrragesTask());
		closeBottomMenu();
		bottomMenu.getBagrrageBtn().setText("关闭弹幕");
	}
	
	public boolean isBagrrageOpen() {
		return isBagrrageOpen;
	}
	
	public void setIsNoteMenuOpen(boolean state) {
		isNoteMenuOpen = state;
	}
	
	public void closeBagrrage(){
		isBagrrageOpen = false;
		bagrrageLayout.setVisibility(View.GONE);
		handler.sendEmptyMessage(MSG_UPDATE);
		closeBottomMenu();
		bottomMenu.getBagrrageBtn().setText("打开弹幕");
	}

	public void updateBookName() {
		String name = book.getBookName();
		bookNameView.setText(name);
	}
	
	public void updateChapterName() {
		String name = contentView.getCurrentChapter().getChapterName();
		chapterNameView.setText(name);
	}
	
	public void updateProcess() {
		Chapter chapter = contentView.getCurrentChapter();
		float process = (float) (chapter.getPageNum()+1)/chapter.getPageLength();
		String str = new DecimalFormat("##.#").format(process*100)+"%";
		processView.setText(str);
	}

	private Chapter mkChapter(Book book){
		Chapter chapter = null;
		//获取表chapter信息
		Cursor cursor = MainActivity.database.rawQuery("select * from "
				+ DatabaseHelper.TABLE_CHAPTER + " where id=? and num=?", 
				new String[]{"" + bookId, "" + book.getChapterNum()});
		if(cursor.moveToFirst()) {
			String chapterName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAPTER_NAME));
			String chapterContent = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CHAPTER_CONTENT));
			chapter = new Chapter(book, chapterName, chapterContent);
		}
		return chapter;
	}

	//向数据库存入一章
	private void saveChapter(String name, String content, int chapterNum){
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.BOOK_ID, bookId);
		cv.put(DatabaseHelper.CHAPTER_NUM, chapterNum);
		cv.put(DatabaseHelper.CHAPTER_NAME, name);
		cv.put(DatabaseHelper.CHAPTER_CONTENT, content);
		MainActivity.database.insert(DatabaseHelper.TABLE_CHAPTER, null, cv);
		book.setTotalChapterNum(book.getTotalChapterNum()+1);
		cv.clear();
		cv.put(DatabaseHelper.BOOK_TOTAL_CHAPTER_NUM, book.getTotalChapterNum());
		MainActivity.database.update(DatabaseHelper.TABLE_BOOK, cv, "id=" + bookId, null);
	}
	
	//是否显示状态栏
	private void setScreenStyle(boolean flag){
	    if (flag) { //隐藏状态栏
	        WindowManager.LayoutParams lp = getWindow().getAttributes();
	        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
	        getWindow().setAttributes(lp);
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	    } else { //显示状态栏
	        WindowManager.LayoutParams lp = getWindow().getAttributes();
	        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        getWindow().setAttributes(lp);
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	    }
	}
	
	private void nextPage() {
		if(!contentView.nextPage()) {
			if (nextChapter()) {
				netThread.execute(new AskPageBagrragesTask());
			}
		} else if (isBagrrageOpen) {
			netThread.execute(new AskPageBagrragesTask());
		}
	}

	private void lastPage() {
		if(!contentView.lastPage()) {
			if (lastChapter()) {
				netThread.execute(new AskPageBagrragesTask());
			}
		} else if (isBagrrageOpen) {
			netThread.execute(new AskPageBagrragesTask());
		}
	}
	
	private boolean nextChapter() {
		autoDownloadTime = SHORT;
		
		if(book.getChapterNum() < book.getTotalChapterNum()-1) {
			book.setChapterNum(book.getChapterNum()+1);
			book.setPageNum(0);
			Chapter chapter = mkChapter(book);
			contentView.setChapter(chapter, 0);
			return true;
		}
		return false;
	}
	
	private boolean lastChapter() {
		autoDownloadTime = SHORT;
		
		if(book.getChapterNum() > 0) {
			book.setChapterNum(book.getChapterNum()-1);
			book.setPageNum(0);
			Chapter chapter = mkChapter(book);
			contentView.setChapter(chapter, ContentView.END_PAGE);
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX()-contentView.getX();
		float y = event.getY()-contentView.getY()-topLayout.getHeight();
		int pageStart = contentView.getCurrentPage().getStart();
		
		switch(event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (isChoosing && !isChanging) {
				endWord = contentView.findWord(x, y);
				contentView.setChoosingLine(new Line(pageStart, startWord, endWord));
				contentView.invalidate();
			} else if (isChoosing && isChanging) {
				Word word = contentView.findWord(x, y);
				//如果选中的是第一个三角形
				if(isChangeStart){
					Line line = new Line(pageStart, word, endWord);
					contentView.setChoosingLine(line);
					contentView.invalidate();
				}
				//如果选中的是第二个三角形
				if(isChangeEnd){
					Line line = new Line(pageStart, startWord, word);
					contentView.setChoosingLine(line);
					contentView.invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(isChoosing && !isChanging){
				isChanging = true;
			}
			else if(isChoosing && isChanging){
				startWord = contentView.findWord(x, y);
				endWord = contentView.findWord(x, y);
				isChangeStart = false;
				isChangeEnd = false;
			}
			break;
		}
		
		return detector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		final View v = this.getWindow().peekDecorView();
		if (v != null && v.getWindowToken() != null) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		if(isChoosing && isChanging){
			float x = e.getX()-contentView.getX();
			float y = e.getY()-contentView.getY()-topLayout.getHeight();
			Word startWord = contentView.getCurrentPage().getWordsList()
					.get(contentView.getChoosingLine().getStart());
			Word endWord = contentView.getCurrentPage().getWordsList()
					.get(contentView.getChoosingLine().getEnd());
			if(x >= startWord.getX()-style.getTextSize()*1.5
					&& x <= startWord.getX()+style.getTextSize()*0.5
					&& y >= startWord.getY()-style.getTextSize()
					&& y <= startWord.getY()+style.getTextSize()){
				isChangeStart = true;
				this.endWord = contentView.getCurrentPage().getWordsList()
						.get(contentView.getChoosingLine().getEnd());
			}else if(x >= endWord.getX()+style.getTextSize()*0.5
					&& x <= endWord.getX()+style.getTextSize()*2.5
					&& y >= endWord.getY()-style.getTextSize()
					&& y <= endWord.getY()+style.getTextSize()){
				isChangeEnd = true;
				this.startWord = contentView.getCurrentPage().getWordsList()
						.get(contentView.getChoosingLine().getStart());
			}
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		float x = e.getX()-contentView.getX();
		float y = e.getY()-contentView.getY()-topLayout.getHeight();
		if (isChoosing && isChanging) {
			contentView.getCurrentPage().addNote(contentView.getChoosingLine(),
					bookId, book.getChapterNum());
			contentView.setChoosingLine(null);
			isChoosing = false;
			isChanging = false;
			contentView.invalidate();
		} else {
			Word word = contentView.findWordOrNull(x, y);
			Note note = null;
			Line line = null;
			if(word != null && word.isInNote()) {
				note = word.getBelongNote();
				line = new Line(contentView.getCurrentPage().getStart(), note);
			}
			float width = readLayout.getWidth();
			float height = readLayout.getHeight();
			float parentX = e.getX();
			float parentY = e.getY();
			boolean isOpenLineMenu = line != null;
			boolean isCenter = (parentX >= width/3 && parentX <= width*2/3
					&& parentY >= height/3 && parentY <= height*2/3);
			boolean isLeft = (parentX <= width*2/3 && parentY <= height*2/3 && !isCenter);
			boolean isRight = (!isCenter && !isLeft);
			boolean isOpenBottomMenu = (isCenter && !isOpenLineMenu);
			boolean isLastPage = (isLeft && !isOpenLineMenu);
			boolean isNextPage = (isRight && !isOpenLineMenu);
			if (isNoteMenuOpen) {
				closeNoteMenu();
			} else if(isBottomMenuOpen) {
				closeBottomMenu();
			} else if (isOpenLineMenu) {
				openNoteMenu(note);
			} else if (isOpenBottomMenu) {
				openBottomMenu();
			} else if (isLastPage) {
				lastPage();
			} else if (isNextPage) {
				nextPage();
			}
		}
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		float x = e.getX()-contentView.getX();
		float y = e.getY()-contentView.getY()-topLayout.getHeight();
		startWord = contentView.findWord(x, y);
		contentView.setChoosingLine(new Line(contentView.getCurrentPage().getStart(), startWord));
		contentView.invalidate();
		isChoosing = true;
		isChanging = false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(!isChanging && !isChoosing) {
			if(isNoteMenuOpen) {
				closeNoteMenu();
			} else if(isBottomMenuOpen) {
				closeBottomMenu();
			} else if(velocityX > 0) {
				lastPage();
			} else if(velocityX < 0) {
				nextPage();
			}
		} else if (isChoosing && isChanging) {
			contentView.getCurrentPage().addNote(contentView.getChoosingLine(),
					bookId, book.getChapterNum());
			contentView.setChoosingLine(null);
			isChoosing = false;
			isChanging = false;
			contentView.invalidate();
		}
		return true;
	}
	
	public void openNoteMenu(Note note) {
		String s = contentView.getCurrentChapter().getChapterContent()
				.substring(note.getStart(), note.getEnd()+1);

		noteMenu.show(note, s);
		isNoteMenuOpen = true;
	}
	
	public void closeNoteMenu(){
		noteMenu.close();
		isNoteMenuOpen = false;
	}
	
	public void openBottomMenu() {
		bottomMenu.show();
		isBottomMenuOpen = true;
	}
	
	public void closeBottomMenu() {
		bottomMenu.close();
		isBottomMenuOpen = false;
	}
	
	public ContentView getContentView() {
		return contentView;
	}

	private void updatePageBagrrages(String strBagrrages){
		synchronized (bagrrages) {
			if(strBagrrages.length() != 0){
				this.bagrrages.clear();
				String[] s = strBagrrages.split("\n");
				for(int i = 0; i < s.length; i++){
					this.bagrrages.add(s[i]);
				}
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private class ActivityHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_UPDATE:
				contentView.update();
				break;
			case MSG_SHOW_BAGRRAGE:
				String bagrrage = null;
				synchronized (bagrrages) {
					if(bagrrages.isEmpty()) {
						break;
					} else {
						bagrrage = bagrrages.removeFirst();
					}
				}
				int duration = 5000 + (int) (Math.random()*2000);
				int num = (int) (Math.random()*BAGRRAGE_PATH_NUM);		//随机选一行显示
				BagrrageView bagrrageView = new BagrrageView(ReadActivity.this,bagrrage,
						duration, bagrragePathViews[num]);
				bagrragePathViews[num].addView(bagrrageView);
				bagrrageView.start();
				break;
			case MSG_SHOW_TIME:
				String s = new DecimalFormat("#.#").format(time/60f);
				Toast.makeText(getApplicationContext(), "您已连续阅读" + s + "小时，适当的休息能使效率更高哦", 
						Toast.LENGTH_LONG).show();
				break;
			}
		}
		
	}
	
	private class ClockThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(60000);
				time++;
				if(time > 0 && time%30 == 0) {
					handler.sendEmptyMessage(MSG_SHOW_TIME);
				}
			} catch (Exception e) {
				
			}
		}
	}
	
	private class AutoDownloadThread extends Thread {
		@Override
		public void run() {
			while(isAutoDownload){
				if(book.getChapterNum()+chapterBufferSize > book.getTotalChapterNum()) {
					netThread.execute(new DownloadChapterTask());
				}
				try {
					Thread.sleep(autoDownloadTime);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	private class ShowBagrrageThread extends Thread {
		@Override
		public void run() {
			while(isBagrrageOpen) {
				try {
					int time = 2000+(int) (Math.random()*1000);
					Thread.sleep(time);
					handler.sendEmptyMessage(MSG_SHOW_BAGRRAGE);
				} catch (Exception e) {
				}
			}
		}
	}
	
	private class NetThread extends Thread{
		private LinkedList<NetTask> tasksList = new LinkedList<NetTask>();
		
		@Override
		public void run(){
			NetTask task;
			while(MainActivity.isConnected()){
				synchronized (tasksList) {
					while(tasksList.isEmpty()){
						try{
							tasksList.wait();
						} catch (InterruptedException e) {
							
						}
					}
					task = tasksList.removeFirst();
				}
				try {
					task.run();
				} catch (Exception e) {
					
				}
			}
		}
		public void execute(NetTask task){
			if(MainActivity.isConnected()){
				synchronized (tasksList) {
					if(!tasksList.isEmpty() && task.priority > tasksList.getFirst().priority){
						tasksList.addFirst(task);
					}else{
						tasksList.addLast(task);
					}
					tasksList.notify();
				}
			}
		}
	}
	
	private class NetTask implements Runnable {
		public int priority;
		public void run(){
			
		}
	}
	
	private class SendBagrrageTask extends NetTask {
		
		public SendBagrrageTask(){
			priority = 3;
		}

		@Override
		public void run() {
			synchronized (MainActivity.dos) {
				DataOutputStream dos = MainActivity.dos;
				try {
					dos.writeInt(MainActivity.SIGN_SEND_BAGRRAGE);
					dos.writeInt(bookId);
					dos.writeInt(book.getChapterNum());
					dos.writeInt(contentView.getCurrentPage().getStart());
					dos.writeInt(contentView.getCurrentPage().getEnd());
					dos.writeUTF(bagrrageEditText.getText().toString());
					dos.flush();
				} catch (Exception e) {
				}
			}
		}
		
	}
	
	private class AskPageBagrragesTask extends NetTask {
		public AskPageBagrragesTask(){
			priority = 2;
		}

		public void run(){
			synchronized (MainActivity.dos) {
				DataOutputStream dos = MainActivity.dos;
				DataInputStream dis = MainActivity.dis;
				try {
					synchronized (bagrrages) {
						bagrrages.clear();
					}
					dos.writeInt(MainActivity.SIGN_ASK_PAGE_BAGRRAGES);
					dos.writeInt(bookId);
					dos.writeInt(book.getChapterNum());
					dos.writeInt(contentView.getCurrentPage().getStart());
					dos.flush();
					String strBagrrages = dis.readUTF();
					updatePageBagrrages(strBagrrages);
				} catch (Exception e) {
				}
			}
		}
	}
	
	private class DownloadChapterTask extends NetTask {
		
		public DownloadChapterTask(){
			priority = 1;
		}
		
		public void run(){
			DataOutputStream dos = MainActivity.dos;
			DataInputStream dis = MainActivity.dis;
			try {
				dos.writeInt(MainActivity.SIGN_DOWNLOAD_CHAPTER);
				dos.writeInt(bookId);
				dos.writeInt(book.getTotalChapterNum());
				dos.flush();
				String name = dis.readUTF();
				String content = dis.readUTF();
				if(name.length() != 0) {
					saveChapter(name, content, book.getTotalChapterNum());
				}else{
					autoDownloadTime = LONG;
				}
			} catch (Exception e) {
			}
		}
	}
	
	private void saveAppInfo() {
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.APP_TEXT_SIZE, Tools.px2sp(this, style.getTextSize()));
		cv.put(DatabaseHelper.APP_TEXT_COLOR, style.getTextColor());
		cv.put(DatabaseHelper.APP_BACKGROUND_COLOR, style.getBackgroundColor());
		cv.put(DatabaseHelper.APP_TEXT_TYPEFACE_PATH, style.getTypefaceStr());
		cv.put(DatabaseHelper.APP_COLOR_STYLE, style.getColorStyle());
		cv.put(DatabaseHelper.APP_IS_BAGRRAGE_OPEN, isBagrrageOpen);
		MainActivity.database.update(DatabaseHelper.TABLE_APP, cv, "", null);
	}
	
	private void saveBookInfo() {
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.BOOK_LAST_CHAPTER_NUM, book.getChapterNum());
		cv.put(DatabaseHelper.BOOK_LAST_PAGE_NUM, book.getPageNum());
		MainActivity.database.update(DatabaseHelper.TABLE_BOOK,
				cv, "id=?", new String[]{"" + bookId});
	}
	
	private void closeAllThread(){
		isAutoDownload = false;
		isBagrrageOpen = false;
		try {
			autoThread.interrupt();
		} catch (Exception e) {
			
		}
		try {
			showBagrrageThread.interrupt();
		} catch (Exception e) {
			
		}
		try {
			netThread.interrupt();
		} catch (Exception e) {
			
		}
		try {
			clockThread.interrupt();
		} catch (Exception e) {
			
		}
	}

	@Override
	protected void onDestroy() {
		saveAppInfo();
		saveBookInfo();
		closeAllThread();
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		if(isBagrrageOpen) {
			openBagrrage();
		}
		contentView.update();
		super.onRestart();
	}
	
}