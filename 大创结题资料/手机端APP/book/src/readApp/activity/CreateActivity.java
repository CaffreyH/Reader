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
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class CreateActivity extends Activity {
	
	private EditText idEdit;
	private EditText passwordEdit;
	private EditText nameEdit;
	private RadioGroup sexRadio;
	private Button backBtn;
	private Button createBtn;
	
	private String id;
	private String password;
	private String name;
	private String sex = "ƒ–";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		
		initView();
		
	}

	private void initView() {
		idEdit = (EditText) findViewById(R.id.id_edit);
		passwordEdit = (EditText) findViewById(R.id.password_edit);
		nameEdit = (EditText) findViewById(R.id.name_edit);
		sexRadio = (RadioGroup) findViewById(R.id.sex_radio);
		backBtn = (Button) findViewById(R.id.back_btn);
		createBtn = (Button) findViewById(R.id.create_btn);
		
		createBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				id = idEdit.getText().toString();
				password = passwordEdit.getText().toString();
				name = nameEdit.getText().toString();
				
				if(id.isEmpty()) {
					Toast.makeText(getApplicationContext(), "«Î ‰»Î’À∫≈", 
							Toast.LENGTH_SHORT).show();
				} else if(password.isEmpty()) {
					Toast.makeText(getApplicationContext(), "«Î ‰»Î√‹¬Î", 
							Toast.LENGTH_SHORT).show();
				} else if(name.isEmpty()) {
					Toast.makeText(getApplicationContext(), "«Î ‰»ÎÍ«≥∆", 
							Toast.LENGTH_SHORT).show();
				} else {
					createUser();
					
					Toast.makeText(getApplicationContext(), "◊¢≤·≥…π¶", 
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.setClass(CreateActivity.this, ShelfActivity.class);
					startActivity(intent);
					MainActivity.isLogin = true;
				}
			}
			
		});
		
		backBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
		sexRadio.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.man) {
					sex = "ƒ–";
				} else {
					sex = "≈Æ";
				}
			}
			
		});
	}

	private void createUser() {
		ContentValues cv = new ContentValues();
		
		cv.put(DatabaseHelper.APP_USER_ID, id);
		cv.put(DatabaseHelper.APP_USER_PASSWORD, password);
		cv.put(DatabaseHelper.APP_USER_NAME, name);
		cv.put(DatabaseHelper.APP_USER_SEX,sex);
		
		MainActivity.database.update(DatabaseHelper.TABLE_APP, cv, "", null);
	}
}
