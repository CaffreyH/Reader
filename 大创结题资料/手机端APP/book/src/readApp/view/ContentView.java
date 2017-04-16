package readApp.view;

import java.util.ArrayList;

import readApp.activity.MainActivity;
import readApp.activity.ReadActivity;
import readApp.book.Book;
import readApp.book.Chapter;
import readApp.book.Line;
import readApp.book.Note;
import readApp.book.Page;
import readApp.book.Word;
import readApp.support.Style;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class ContentView extends View {
	private final float TITLE_LINE_STROKE = 3;		//标题分割线的宽度
	private final float MARKLINE_WORD_SPACING = 10;		//标记线距离文字的距离
	private final float MARKLINE_STROKE = 4;		//标记线宽度
	private final int MARKLINE_COLOR = Color.rgb(0, 127, 255);		//标记线的颜色
	private final float TRIANGLE_WIDTH = 25;		//临时线两端三角形的宽
	private final float TRIANGLE_HEIGHT = 10;		//临时线两端三角形的半高
	private final float TRIANGLE_LINE_SPACING = 10;		//三角形与线的距离
	
	public static final int END_PAGE = -1;
	
	private ReadActivity activity;
	
	private int width;
	private int height;
	private Book book;
	private Chapter currentChapter;
	private Page currentPage;
	private Line choosingLine;
	private Style style;

	private Paint textPaint;		//内容画笔
	private Paint titlePaint;		//标题画笔
	private Paint linePaint;		//标记线画笔

	public ContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (ReadActivity) this.getContext();
		init();
	}
	
	private void init(){
		initPaint();
	}
	
	private void initPaint(){
		textPaint = new Paint();
		titlePaint = new Paint();
		linePaint = new Paint();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		if(currentChapter != null && !currentChapter.isAlive()) {
			currentChapter.updatePages(width, height, style.getTextSize(),
					style.getTextSize()*MainActivity.LINE_SPACING,
					style.getTextSize()*MainActivity.PARA_SPACING);
			activity.updateProcess();
			updateCurrntPage(currentChapter.pageAt(currentChapter.getPageNum()));
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawTitle(canvas);
		drawPageWord(canvas);
		drawChoosingLine(canvas);
		drawNotes(canvas);
	}

	private void drawPageWord(Canvas canvas){
		if(currentChapter != null && currentChapter.isAlive()){
			for(int i = 0; i < currentPage.getWordsList().size(); i++){
				Word word = currentPage.getWordsList().get(i);
				canvas.drawText("" + word.getWord(), word.getX(), word.getY(), textPaint);
			}
		}
	}
	
	private void drawTitle(Canvas canvas) {
		if(currentChapter != null && book.getPageNum() == 0) {
			//写标题
			canvas.drawText(currentChapter.getChapterName(), 0,
					ReadActivity.titleTextHeight, titlePaint);
			//画线
			canvas.drawLine(0, ReadActivity.titleLineHeight, 
					width, ReadActivity.titleLineHeight, 
					titlePaint);
		}
	}
	
	private void drawChoosingLine(Canvas canvas){
		if(choosingLine != null) {
			//画线
			for(int i = choosingLine.getStart(); i <= choosingLine.getEnd(); i++){
				Word word = currentPage.getWordsList().get(i);
				canvas.drawLine(word.getX(), 
						word.getY()+MARKLINE_WORD_SPACING, 
						word.getX()+word.getWidth(), 
						word.getY()+MARKLINE_WORD_SPACING, linePaint);
			}
			//画出临时线两端的标记三角形
			//起始三角形
			Word startWord = currentPage.getWordsList().get(choosingLine.getStart());
			Path triangleStart = new Path();
			triangleStart.moveTo(startWord.getX()-TRIANGLE_LINE_SPACING, 
					startWord.getY()+MARKLINE_WORD_SPACING);
			triangleStart.lineTo(startWord.getX()-TRIANGLE_LINE_SPACING-TRIANGLE_WIDTH, 
					startWord.getY()+TRIANGLE_HEIGHT+MARKLINE_WORD_SPACING);
			triangleStart.lineTo(startWord.getX()-TRIANGLE_LINE_SPACING-TRIANGLE_WIDTH, 
					startWord.getY()-TRIANGLE_HEIGHT+MARKLINE_WORD_SPACING);
			triangleStart.close();
			canvas.drawPath(triangleStart, linePaint);
			//结束三角形
			Word endWord = currentPage.getWordsList().get(choosingLine.getEnd());
			Path triangleEnd = new Path();
			triangleEnd.moveTo(endWord.getX()+TRIANGLE_LINE_SPACING+endWord.getWidth(), 
					endWord.getY()+MARKLINE_WORD_SPACING);
			triangleEnd.lineTo(endWord.getX()+TRIANGLE_LINE_SPACING+TRIANGLE_WIDTH+endWord.getWidth(), 
					endWord.getY()+TRIANGLE_HEIGHT+MARKLINE_WORD_SPACING);
			triangleEnd.lineTo(endWord.getX()+TRIANGLE_LINE_SPACING+TRIANGLE_WIDTH+endWord.getWidth(), 
					endWord.getY()-TRIANGLE_HEIGHT+MARKLINE_WORD_SPACING);
			triangleEnd.close();
			canvas.drawPath(triangleEnd, linePaint);
		}
	}
	
	private void drawNotes(Canvas canvas) {
		if(currentPage != null) {
			for(int i = 0; i < currentPage.getWordsList().size(); i++) {
				Word word = currentPage.getWordsList().get(i);
				if(word.isInNote()) {
					canvas.drawLine(word.getX(), 
							word.getY()+MARKLINE_WORD_SPACING, 
							word.getX()+word.getWidth(), 
							word.getY()+MARKLINE_WORD_SPACING, linePaint);
				}
			}
		}
	}
	
	public void setBook(Book book) {
		this.book = book;
	}

	public void setChapter(Chapter chapter, int num){
		currentChapter = chapter;
		if(width != 0) {
			currentChapter.updatePages(width, height, style.getTextSize(),
					style.getTextSize()*MainActivity.LINE_SPACING,
					style.getTextSize()*MainActivity.PARA_SPACING);
			activity.updateProcess();
			if(num >= 0) {
				updateCurrntPage(currentChapter.pageAt(num));
			} else {
				updateCurrntPage(currentChapter.endPage());
			}
		}
		activity.updateChapterName();
		invalidate();
	}
	
	public void setStyle(Style style){
		this.style = style;
		textPaint.setTextSize(style.getTextSize());
		textPaint.setColor(style.getTextColor());
		textPaint.setTypeface(style.getTypeface());
		textPaint.setAntiAlias(true);
		
		titlePaint.setTextSize(style.getTextSize()+5);
		titlePaint.setColor(style.getTextColor());
		titlePaint.setTypeface(Typeface.create(style.getTypeface(), Typeface.BOLD));
		titlePaint.setStrokeWidth(TITLE_LINE_STROKE);
		titlePaint.setAntiAlias(true);

		linePaint.setStrokeWidth(MARKLINE_STROKE);
		linePaint.setColor(MARKLINE_COLOR);
		invalidate();
	}
	
	public boolean nextPage(){
		Page page = currentChapter.nextPage();
		if(page != null){
			updateCurrntPage(page);
			activity.updateProcess();
			invalidate();
			return true;
		}
		return false;
	}
	
	public boolean lastPage(){
		Page page = currentChapter.lastPage();
		if(page != null){
			updateCurrntPage(page);
			activity.updateProcess();
			invalidate();
			return true;
		}
		return false;
	}
	
	public Chapter getCurrentChapter() {
		return currentChapter;
	}

	public Page getCurrentPage() {
		return currentPage;
	}
	
	public Line getChoosingLine() {
		return choosingLine;
	}

	public void setChoosingLine(Line choosingLine) {
		this.choosingLine = choosingLine;
	}
	
	private void updateCurrntPage(Page page) {
		currentPage = page;
		currentPage.updateNotedWords(currentChapter.getPageNotes(currentPage));
	}

	public Word findWord(float x, float y) {
		ArrayList<ArrayList<Word>> wordsList2d = currentPage.getWordsList2d();
		ArrayList<Word> row = null;
		Word word = null;
		for(int i = 0; i < wordsList2d.size(); i++){
			if(wordsList2d.get(i).get(0).getY() >= y){
				row = wordsList2d.get(i);
				break;
			}
		}
		if(row == null){
			row = wordsList2d.get(wordsList2d.size()-1);
		}
		for(int i = 0; i < row.size(); i++){
			if(row.get(i).getX()+row.get(i).getWidth() >= x){
				word = row.get(i);;
				break;
			}
		}
		if(word == null){
			word = row.get(row.size()-1);
		}
		return word;
	}
	
	public Word findWordOrNull(float x, float y) {
		ArrayList<Word> wordsList = currentPage.getWordsList();
		Word word = null;
		for(int i = 0; i < wordsList.size(); i++) {
			Word word0 = wordsList.get(i);
			if(y <= word0.getY() && y >= word0.getY()-word0.getHeight()
					&& x >= word0.getX() && x <= word0.getX()+word0.getWidth()) {
				word = word0;
				break;
			}
		}
		return word;
	}
	
	public void deleteNote(Note note) {
		currentPage.deleteNote(note);
		invalidate();
	}
	
	public void update() {
		Chapter chapter = new Chapter(currentChapter);
		chapter.updatePages(width, height, style.getTextSize(),
				style.getTextSize()*MainActivity.LINE_SPACING,
				style.getTextSize()*MainActivity.PARA_SPACING);
		setChapter(chapter, book.getPageNum());
	}
}
