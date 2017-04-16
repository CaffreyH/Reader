package readApp.book;

public class Word {
	private float x;
	private float y;
	private float width;
	private float height;
	private char word;
	private int location;
	private Note belongNote;
	
	public Word(float x, float y, float width, float height, char word, int location) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.word = word;
		this.location = location;
		belongNote = null;
	}
	
	public boolean isInNote(){
		return belongNote != null;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
	
	public char getWord(){
		return word;
	}

	public Note getBelongNote() {
		return belongNote;
	}

	public int getLocation() {
		return location;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public void setBelongNote(Note belongNote) {
		this.belongNote = belongNote;
	}

	public void setLocation(int location) {
		this.location = location;
	}
	
}
