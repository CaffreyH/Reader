package readApp.support;

import android.graphics.Typeface;

public class Style {
	private int textSize;
	private Typeface typeface;
	private String typefaceStr;
	private int textColor;
	private int backgroundColor;
	private int colorStyle;
	public Style(int textSize, Typeface typeface, int textColor,
			int backgroundColor, int colorStyle) {
		this.textSize = textSize;
		this.typeface = typeface;
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
		this.colorStyle = colorStyle;
	}
	public int getTextSize() {
		return textSize;
	}
	public Typeface getTypeface() {
		return typeface;
	}
	public int getTextColor() {
		return textColor;
	}
	public int getBackgroundColor() {
		return backgroundColor;
	}
	public int getColorStyle() {
		return colorStyle;
	}
	public String getTypefaceStr() {
		return typefaceStr;
	}
	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}
	public void setTypeface(Typeface typeface) {
		this.typeface = typeface;
	}
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public void setColorStyle(int colorStyle) {
		this.colorStyle = colorStyle;
	}
	public void setTypefaceStr(String typefaceStr) {
		this.typefaceStr = typefaceStr;
	}
}
