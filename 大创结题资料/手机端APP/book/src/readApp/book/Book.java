package readApp.book;

import readApp.support.Tools;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class Book {
	private int bookId;
	private String bookName;
	private String bookWriter;
	private String bookState;
	private String bookIntro;
	private Drawable bookImage;
	
	private int totalChapterNum;
	private int chapterNum;
	private int pageNum;
	
	public Book(int bookId, String bookName,String bookWriter,
			String bookState, String bookIntro,
			int totalChapterNum, int chapterNum, int pageNum) {
		super();
		this.bookId = bookId;
		this.bookName = bookName;
		this.bookWriter = bookWriter;
		this.bookState = bookState;
		this.bookIntro = bookIntro;
		this.totalChapterNum = totalChapterNum;
		this.chapterNum = chapterNum;
		this.pageNum = pageNum;
	}

	public Book(int bookId, String bookName, int totalChapterNum, int chapterNum, int pageNum) {
		super();
		this.bookId = bookId;
		this.bookName = bookName;
		this.totalChapterNum = totalChapterNum;
		this.chapterNum = chapterNum;
		this.pageNum = pageNum;
	}

	public int getBookId() {
		return bookId;
	}

	public String getBookName() {
		return bookName;
	}

	public String getBookWriter() {
		return bookWriter;
	}

	public String getBookState() {
		return bookState;
	}

	public String getBookIntro() {
		return bookIntro;
	}

	public int getTotalChapterNum() {
		return totalChapterNum;
	}

	public int getChapterNum() {
		return chapterNum;
	}
	
	public int getPageNum() {
		return pageNum;
	}

	public Drawable getBookImage() {
		return bookImage;
	}
	
	public byte[] getImageBytes(){
		return Tools.drawable2bytes(bookImage);
	}
	
	public void setChapterNum(int chapterNum) {
		this.chapterNum = chapterNum;
	}
	
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	
	public synchronized void setTotalChapterNum(int num){
		this.totalChapterNum = num;
	}
	
	public void setBookImage(Drawable bookImage) {
		this.bookImage = bookImage;
	}

	public void setBookImage(Context context, byte[] image){
		this.bookImage = Tools.bytes2drawable(context, image);
	}
}
