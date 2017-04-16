package readApp.view;

import readApp.activity.MainActivity;
import readApp.activity.NoteActivity;
import readApp.activity.R;
import readApp.activity.ReadActivity;
import readApp.book.Note;
import readApp.sqlite.DatabaseHelper;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NoteMenu extends LinearLayout {
	private ImageView noteView;
	private ImageView copyView;
	private ImageView deleteView;
	
	private ReadActivity activity;
	
	private Animation showAnim;
	private Animation closeAnim;

	private String lineWords;
	private Note note;
	
	public NoteMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (ReadActivity) context;
		
		this.setVisibility(View.INVISIBLE);
		initViews();
	}

	@SuppressLint("NewApi")
	private void initViews(){
		this.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
		params.setMargins(0,  20, 0, 20);
		
		noteView = new ImageView(this.getContext());
		noteView.setImageDrawable(this.getContext().getDrawable(R.drawable.note_select));
		noteView.setLayoutParams(params);
		this.addView(noteView);
		
		copyView = new ImageView(this.getContext());
		copyView.setImageDrawable(this.getContext().getDrawable(R.drawable.copy_select));
		copyView.setLayoutParams(params);
		this.addView(copyView);
		
		deleteView = new ImageView(this.getContext());
		deleteView.setImageDrawable(this.getContext().getDrawable(R.drawable.delete_select));
		deleteView.setLayoutParams(params);
		this.addView(deleteView);
		
		initViewsEvent();
		initAnim();
	}
	
	private void initAnim(){
		showAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.show_note_anim);
		closeAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.close_note_anim);
		
		closeAnim.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationStart(Animation animation) {
				
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
		});
	}
	
	@SuppressLint("ClickableViewAccessibility")
	private void initViewsEvent(){
		noteView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("lineWords", lineWords);
				intent.putExtra("bookId", note.getBookId());
				intent.putExtra("chapterNum", note.getChapterNum());
				intent.putExtra("start", note.getStart());
				intent.putExtra("end", note.getEnd());
				intent.putExtra("content", note.getContent());
				intent.setClass(getContext(), NoteActivity.class);
				getContext().startActivity(intent);
				setVisibility(View.INVISIBLE);
				activity.setIsNoteMenuOpen(false);
				close();
			}
			
		});
		
		copyView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ClipboardManager myClipboard = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData myClip = ClipData.newPlainText("text", lineWords);
				myClipboard.setPrimaryClip(myClip);
				Toast.makeText(getContext().getApplicationContext(), "ÒÑ¸´ÖÆµ½Õ³Ìù°å", 
						Toast.LENGTH_SHORT).show();
				activity.closeNoteMenu();
			}
			
		});
		
		deleteView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				deleteNote(note);
				Toast.makeText(getContext().getApplicationContext(), "É¾³ý±Ê¼Ç", 
						Toast.LENGTH_SHORT).show();
				activity.closeNoteMenu();
			}
			
		});
	}
	
	public void show(Note note, String lineWords) {
		this.setVisibility(View.VISIBLE);
		this.note = note;
		this.lineWords = lineWords;
		this.startAnimation(showAnim);
	}
	
	public void close(){
		this.startAnimation(closeAnim);
	}

	@Override
	protected void onDraw(Canvas canvas) {
	}
	
	private void deleteNote(Note note) {
		MainActivity.database.delete(DatabaseHelper.TABLE_NOTE, "id=? and num=? and start=?",
				new String[]{"" + note.getBookId(), "" + note.getChapterNum(), "" + note.getStart()});
		activity.getContentView().deleteNote(note);
	}

	public ImageView getNoteView() {
		return noteView;
	}

	public ImageView getCopyView() {
		return copyView;
	}

	public ImageView getDeleteView() {
		return deleteView;
	}
}
