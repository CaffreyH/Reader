package readApp.book;

import java.util.ArrayList;

import readApp.activity.MainActivity;
import readApp.sqlite.DatabaseHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class Page {
	private int start;
	private int end;
	private ArrayList<Word> wordsList;
	private ArrayList<ArrayList<Word>> wordsList2d;
	
	public Page() {
		wordsList = new ArrayList<Word>();
		wordsList2d = new ArrayList<ArrayList<Word>>();
	}
	
	public void addWord(Word word){
		wordsList.add(word);
	}

	public void update2dList() {
		ArrayList<Word> row = new ArrayList<Word>();
		row.add(wordsList.get(0));
		for(int i = 1; i < wordsList.size(); i++) {
			if(wordsList.get(i).getY() != row.get(0).getY()) {
				wordsList2d.add(row);
				row = new ArrayList<Word>();
				row.add(wordsList.get(i));
				continue;
			}
			row.add(wordsList.get(i));
		}
		wordsList2d.add(row);
	}

	public void addNote(Line line, int bookId, int chapterNum){
		Note note = new Note(bookId, chapterNum, line, null);
		
		Cursor cursor = MainActivity.database.rawQuery("select * from "
				+ DatabaseHelper.TABLE_NOTE + " where id=? and num=? and end>=? and start<=?", 
				new String[]{"" + bookId,
						"" + chapterNum,
						"" + note.getStart(),
						"" + note.getEnd()});
		String content = "";
		int start = note.getStart();
		int end = note.getEnd();
		if(cursor.moveToFirst()) {
			start = Math.min(start,
					cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOTE_START)));
			end = Math.max(end,
					cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOTE_END)));
			MainActivity.database.delete(DatabaseHelper.TABLE_NOTE, "id=? and num=?"
					+ " and start=?",
					new String[]{"" + bookId, "" + chapterNum,
					"" + cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOTE_START))});
			String s = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOTE_NOTE));
			if(s != null) {
				content += cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOTE_NOTE));
			}
		}
		while(cursor.moveToNext()){
			end = Math.max(note.getEnd(),
					cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOTE_END)));
			MainActivity.database.delete(DatabaseHelper.TABLE_NOTE, "id=? and num=?"
					+ " and end=?",
					new String[]{"" + bookId, "" + chapterNum,
					"" + cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NOTE_END))});
			String s = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOTE_NOTE));
			if(s != null) {
				content += cursor.getString(cursor.getColumnIndex(DatabaseHelper.NOTE_NOTE));
			}
		}
		
		if (content.length() != 0) {
			note = new Note(bookId, chapterNum, start, end, content);
		} else {
			note = new Note(bookId, chapterNum, start, end, null);
		}
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.BOOK_ID, bookId);
		cv.put(DatabaseHelper.CHAPTER_NUM, chapterNum);
		cv.put(DatabaseHelper.NOTE_START, note.getStart());
		cv.put(DatabaseHelper.NOTE_END, note.getEnd());
		if(note.getContent() != null) {
			cv.put(DatabaseHelper.NOTE_NOTE, note.getContent());
		}
		MainActivity.database.insert(DatabaseHelper.TABLE_NOTE, null, cv);
		for(int i = line.getStart(); i <= line.getEnd(); i++) {
			wordsList.get(i).setBelongNote(note);
		}
	}
	
	public void deleteNote(Note note) {
		MainActivity.database.delete(DatabaseHelper.TABLE_NOTE, "id=? and num=? and start=?",
				new String[]{"" + note.getBookId(), "" + note.getChapterNum(), "" + note.getStart()});
		for(int i = note.getStart()-start; i <= note.getEnd()-start; i++) {
			if(i >= 0 && i <= end-start) {
				wordsList.get(i).setBelongNote(null);
			}
		}
	}
	
	public void updateNotedWords(ArrayList<Note> notesList){
		for(int i = 0; i < notesList.size(); i++) {
			Note note = notesList.get(i);
			for(int j = note.getStart(); j <= note.getEnd(); j++) {
				if(j-start >= 0 && j-start < wordsList.size()) {
					wordsList.get(j-start).setBelongNote(note);
				}
			}
		}
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public ArrayList<Word> getWordsList() {
		return wordsList;
	}

	public ArrayList<ArrayList<Word>> getWordsList2d() {
		return wordsList2d;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}
}
