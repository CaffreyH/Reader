package readApp.activity;

import readApp.sqlite.DatabaseHelper;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NoteActivity extends Activity {
	public static final int RUSULTCODE_RETURN_NOTE = 0;
	
	private int bookId;
	private int chapterNum;
	private int start;
	private int end;
	private String lineWords;
	private String content;
	
	private Button saveBtn;
	private EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		
		init();
		TextView tv = (TextView) findViewById(R.id.tv);
		tv.setText(lineWords);
	}
	
	private void init(){
		getDataFromIntent();
		initView();
		initEvent();
	}
	
	private void getDataFromIntent(){
		Intent intent = getIntent();
		bookId = intent.getIntExtra("bookId", -1);
		chapterNum = intent.getIntExtra("chapterNum", -1);
		start = intent.getIntExtra("start", -1);
		end = intent.getIntExtra("end", -1);
		lineWords = intent.getStringExtra("lineWords");
		content = intent.getStringExtra("content");
	}
	
	private void initView(){
		saveBtn = (Button) findViewById(R.id.saveBtn);
		editText = (EditText) findViewById(R.id.editText);
		if(content != null) {
			editText.setText(content);
		}
	}
	
	private void initEvent(){
		saveBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				content = editText.getText().toString();
				
				ContentValues cv = new ContentValues();
				cv.put(DatabaseHelper.NOTE_NOTE, content);
				MainActivity.database.update(DatabaseHelper.TABLE_NOTE,
						cv, "id=? and num=? and start=? and end=?",
						new String[]{"" + bookId, "" + chapterNum, "" + start, "" + end});
				
				NoteActivity.this.finish();
				Toast.makeText(getApplicationContext(), "±£´æ³É¹¦", 
						Toast.LENGTH_SHORT).show();
			}
			
		});
	}
}
