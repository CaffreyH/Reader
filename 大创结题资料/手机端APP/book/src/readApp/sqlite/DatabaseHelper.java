package readApp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String TABLE_BOOK = "book";
	public static final String TABLE_CHAPTER = "chapter";
	public static final String TABLE_APP = "app";
	public static final String TABLE_NOTE = "note";
	
	public static final String BOOK_ID = "id";
	public static final String BOOK_NAME = "name";
	public static final String BOOK_WRITER = "writer";
	public static final String BOOK_STATE = "state";
	public static final String BOOK_INTRODUCTION = "introduction";
	public static final String BOOK_TOTAL_CHAPTER_NUM = "total_chapter_num";
	public static final String BOOK_LAST_CHAPTER_NUM = "last_chapter_num";
	public static final String BOOK_LAST_PAGE_NUM = "last_page_num";
	public static final String BOOK_IMAGE = "image";

	public static final String CHAPTER_NUM = "num";
	public static final String CHAPTER_NAME = "name";
	public static final String CHAPTER_CONTENT = "content";

	public static final String APP_TEXT_SIZE = "text_size";
	public static final String APP_TEXT_TYPEFACE_PATH = "text_typeface_path";
	public static final String APP_IS_BAGRRAGE_OPEN = "integer";
	public static final String APP_COLOR_STYLE = "color_style";
	public static final String APP_BACKGROUND_COLOR = "background_color";
	public static final String APP_TEXT_COLOR = "text_color";
	public static final String APP_FIRST_USE = "first_use";
	public static final String APP_USER_ID = "user_id";
	public static final String APP_USER_PASSWORD = "user_password";
	public static final String APP_USER_NAME = "user_name";
	public static final String APP_USER_SEX = "user_sex";
	
	
	public static final String NOTE_START = "start";
	public static final String NOTE_END = "end";
	public static final String NOTE_NOTE = "note";

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		//create table book
		db.execSQL("create table book("
				+ BOOK_ID + " integer primary key" + ","
				+ BOOK_NAME + " varchar(20)" + ","
				+ BOOK_WRITER + " varchar(20)" + ","
				+ BOOK_STATE + " varchar(20)" + ","
				+ BOOK_INTRODUCTION + " clob" + ","
				+ BOOK_TOTAL_CHAPTER_NUM + " integer" + ","
				+ BOOK_LAST_CHAPTER_NUM + " integer" + ","
				+ BOOK_LAST_PAGE_NUM + " integer" + ","
				+ BOOK_IMAGE + " blob" + ")");
		
		//create table chapter
		db.execSQL("create table chapter("
				+ BOOK_ID + " integer" + ","
				+ CHAPTER_NUM + " integer" + ","
				+ CHAPTER_NAME + " varchar(20)" + ","
				+ CHAPTER_CONTENT + " clob" + ")");
		
		//create table app
		db.execSQL("create table app("
				+ APP_TEXT_SIZE + " integer" + ","
				+ APP_TEXT_TYPEFACE_PATH + " char(20)" + ","
				+ APP_IS_BAGRRAGE_OPEN + " integer" + ","
				+ APP_COLOR_STYLE + " integer" + ","
				+ APP_BACKGROUND_COLOR + " integer" + ","
				+ APP_TEXT_COLOR + " integer" + ","
				+ APP_FIRST_USE + " integer" + ","
				+ APP_USER_ID + " char(20)" + ","
				+ APP_USER_PASSWORD + " char(20)" + ","
				+ APP_USER_NAME + " char(20)" + ","
				+ APP_USER_SEX + " char(2)" + ")");
		
		//create table note
		db.execSQL("create table note("
				+ BOOK_ID + " integer" + ","
				+ CHAPTER_NUM + " integer" + ","
				+ NOTE_START + " integer" + ","
				+ NOTE_END + " integer" + ","
				+ NOTE_NOTE + " clob" + ")");
		
		//½«first_useÉèÎªtrue
		int textSize = 20;
		String textTypefacePath = "fonts/ttf1.ttf";
		int isBagrrageOpen = 0;
		int colorStyle = 0;
		int backgroundColor = Color.rgb(231, 221, 168);
		int textColor = Color.BLACK;
		
		ContentValues cv = new ContentValues();
		cv.put(APP_TEXT_SIZE, textSize);
		cv.put(APP_TEXT_TYPEFACE_PATH, textTypefacePath);
		cv.put(APP_IS_BAGRRAGE_OPEN, isBagrrageOpen);
		cv.put(APP_COLOR_STYLE, colorStyle);
		cv.put(APP_BACKGROUND_COLOR, backgroundColor);
		cv.put(APP_TEXT_COLOR, textColor);
		cv.put(APP_FIRST_USE, 1);
		db.insert(DatabaseHelper.TABLE_APP, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
