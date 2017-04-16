package readApp.book;

public class Line {
	private int pageStart;
	private int start;
	private int end;
	
	public Line(int pageStart, Word startWord, Word endWord) {
		this.pageStart = pageStart;
		start = Math.min(startWord.getLocation(), endWord.getLocation())-pageStart;
		end = Math.max(startWord.getLocation(), endWord.getLocation())-pageStart;
	}
	
	public Line(int pageStart, Word word) {
		this.pageStart = pageStart;
		start = word.getLocation()-pageStart;
		end = word.getLocation()-pageStart;
	}
	
	public Line(int pageStart, Note note) {
		this.pageStart = pageStart;
		start = note.getStart()-pageStart;
		end = note.getEnd()-pageStart;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getPageStart() {
		return pageStart;
	}
}
