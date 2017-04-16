package readApp.book;

import java.util.ArrayList;

import readApp.activity.MainActivity;
import readApp.activity.ReadActivity;
import readApp.sqlite.DatabaseHelper;
import android.database.Cursor;

public class Chapter {
	private Book book;
	private int chapterNum;
	private String chapterName;
	private String chapterContent;
	private ArrayList<Page> pagesList;
	private ArrayList<Note> notesList;
	private int pageNum;
	
	public Chapter(Book book, String chapterName, String chapterContent) {
		super();
		this.book = book;
		this.chapterName = chapterName;
		this.chapterContent = chapterContent;
		chapterNum = book.getChapterNum();
		pageNum = book.getPageNum();
		pagesList = new ArrayList<Page>();
		notesList = new ArrayList<Note>();
		getSQLNotes();
	}
	
	public Chapter(Chapter chapter) {
		super();
		this.book = chapter.getBook();
		this.chapterName = chapter.chapterName;
		this.chapterContent = chapter.chapterContent;
		chapterNum = book.getChapterNum();
		pageNum = book.getPageNum();
		pagesList = new ArrayList<Page>();
		notesList = new ArrayList<Note>();
		getSQLNotes();
	}
	
	public void getSQLNotes(){
		Cursor cursor = MainActivity.database.rawQuery("select * from "
				+ DatabaseHelper.TABLE_NOTE + " where id=? and num=?", 
				new String[]{"" + book.getBookId(), "" + chapterNum});
		while(cursor.moveToNext()) {
			int start = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOTE_START));
			int end = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOTE_END));
			String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOTE_NOTE));
			Note note = new Note(book.getBookId(), chapterNum, start, end, content);
			notesList.add(note);
		}
	}
	
	public ArrayList<Page> updatePages(final float width, final float height,
			final int textSize, final float lineSpacing, final float paraSpacing) {
		int mark = 0;
		int length = chapterContent.length();
		float totalWidth = 0;
		float totalHeight = 0;
		float wordWidth = textSize;
		float wordHeight = textSize;
		ArrayList<Word> lineWord = new ArrayList<Word>();
		while(mark < length){
			totalHeight = textSize;		//总高度初始化
			Page page = new Page();
			page.setStart(mark);
			if(mark == 0){		//如果页是章节标题页
				totalHeight += ReadActivity.titleTextHeight+textSize;
			}
			while(totalHeight+wordHeight+textSize < height && mark < length){
				char c = chapterContent.charAt(mark);
				if(c == '\n'){
					Word word = new Word(totalWidth, totalHeight, 0, wordHeight, c, mark);
					page.addWord(word);
					//总高度增加，总宽度清零
					wordHeight = textSize+lineSpacing+paraSpacing;
					totalHeight += wordHeight;
					totalWidth = 0;
					//指针往前移动
					mark++;
					lineWord = new ArrayList<Word>();
					continue;
				}
				//如果一行总宽度加一个文字宽度超过最大宽度
				if(totalWidth+textSize > width){
					//总高度增加，总宽度清零
					wordHeight = textSize+lineSpacing;
					totalHeight += lineSpacing+textSize;
					totalWidth = 0;
					alignRight(lineWord, width);
					lineWord = new ArrayList<Word>();
					continue;
				}
				//根据字符类型设置字符宽度
				if(c >= 32 && c < 65 || c > 90 && c <=127){
					wordWidth = textSize/2;
				}
				else{
					wordWidth = textSize;
				}
				Word word = new Word(totalWidth, totalHeight, wordWidth, wordHeight, c, mark);
				page.addWord(word);
				lineWord.add(word);
				//指针往前移动
				mark++;
				//文字总宽度增加
				totalWidth += wordWidth;
			}
			//将此页添加到页列表中
			page.setEnd(mark-1);
			page.update2dList();
			addPage(page);
		}
		return pagesList;
	}
	
	public ArrayList<Note> getPageNotes(Page page){
		ArrayList<Note> pageNotes = new ArrayList<Note>();
		for(int i = 0; i < notesList.size(); i++) {
			if(page.getEnd() >= notesList.get(i).getStart()
					&& page.getStart() <= notesList.get(i).getEnd()) {
				pageNotes.add(notesList.get(i));
			}
		}
		return pageNotes;
	}
	
	public boolean isAlive(){
		return pagesList.size() != 0;
	}
	
	public void clearPagesList(){
		pagesList = new ArrayList<Page>();
	}
	
	private void addPage(Page page) {
		pagesList.add(page);
	}
	
	//根据每一行字末尾剩余的长度，调整文字宽度
	private void alignRight(ArrayList<Word> list, float maxWidth){
		Word endWord = list.get(list.size()-1);
		float lineWidth = endWord.getX()+endWord.getWidth();		//一行的原始宽度
		float subWidth = maxWidth-lineWidth;		//一行剩余宽度
		float addWidth = subWidth/list.size();		//每个字将要增加的宽度
		for(int i = 0; i < list.size(); i++){
			Word word = list.get(i);
			word.setWidth(word.getWidth()+addWidth);
			if(i > 0){
				word.setX(list.get(i-1).getX()+list.get(i-1).getWidth());
			}
		}
	}

	public Book getBook() {
		return book;
	}

	public int getChapterNum() {
		return chapterNum;
	}

	public String getChapterName() {
		return chapterName;
	}

	public String getChapterContent() {
		return chapterContent;
	}
	
	public int getPageNum() {
		return pageNum;
	}

	public Page nextPage(){
		if(pageNum+1 < pagesList.size()) {
			pageNum++;
			book.setPageNum(pageNum);
			return pagesList.get(pageNum);
		}
		return null;
	}
	
	public Page lastPage() {
		if(pageNum > 0) {
			pageNum--;
			book.setPageNum(pageNum);
			return pagesList.get(pageNum);
		}
		return null;
	}
	
	public Page startPage(){
		pageNum = 0;
		book.setPageNum(pageNum);
		return pagesList.get(pageNum);
	}
	
	public Page endPage(){
		pageNum = pagesList.size()-1;
		book.setPageNum(pageNum);
		return pagesList.get(pageNum);
	}
	
	public Page pageAt(int pageNum){
		this.pageNum = pageNum;
		book.setPageNum(pageNum);
		return pagesList.get(pageNum);
	}
	
	public int getPageLength(){
		return pagesList.size();
	}
}
