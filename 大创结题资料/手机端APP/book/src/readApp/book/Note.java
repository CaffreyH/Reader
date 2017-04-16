package readApp.book;

public class Note {
	private int bookId;
	private int chapterNum;
	private int start;
	private int end;
	private String content;
	
	public Note(int bookId, int chapterNum, int start, int end, String content) {
		super();
		this.bookId = bookId;
		this.chapterNum = chapterNum;
		this.start = start;
		this.end = end;
		this.content = content;
	}

	public Note(int bookId, int chapterNum, Line line, String content) {
		super();
		this.bookId = bookId;
		this.chapterNum = chapterNum;
		this.start = line.getStart()+line.getPageStart();
		this.end = line.getEnd()+line.getPageStart();
	}

	public int getBookId() {
		return bookId;
	}

	public int getChapterNum() {
		return chapterNum;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getContent() {
		return content;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public void setChapterNum(int chapterNum) {
		this.chapterNum = chapterNum;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
