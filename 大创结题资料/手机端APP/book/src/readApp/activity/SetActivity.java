package readApp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SetActivity extends Activity {
	private Button backBtn;
	private LinearLayout userInfoLayout;
	private LinearLayout notesLayout;
	private LinearLayout textStyleLayout;
	private LinearLayout callUsLayout;
	private LinearLayout versionLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set);
		
		initViews();
	}

	private void initViews() {
		backBtn = (Button) findViewById(R.id.back_btn);
		userInfoLayout = (LinearLayout) findViewById(R.id.user_info);
		notesLayout = (LinearLayout) findViewById(R.id.notes);
		textStyleLayout = (LinearLayout) findViewById(R.id.text_style);
		callUsLayout = (LinearLayout) findViewById(R.id.call_us);
		versionLayout = (LinearLayout) findViewById(R.id.version);
		
		backBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		userInfoLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showToast();
			}
			
		});
		notesLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showToast();
			}
			
		});
		textStyleLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showToast();
			}
			
		});
		callUsLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showToast();
			}
			
		});
		versionLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showToast();
			}
			
		});
		
	}
	
	private void showToast() {
		Toast.makeText(getApplicationContext(), "开发中，敬请期待", 
				Toast.LENGTH_SHORT).show();
	}
}
