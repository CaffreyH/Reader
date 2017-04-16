package readApp.activity;

import readApp.sqlite.DatabaseHelper;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private EditText idEdit;
	private EditText passwordEdit;
	private Button backBtn;
	private Button loginBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		initViews();
	}

	private void initViews() {
		idEdit = (EditText) findViewById(R.id.id_edit);
		passwordEdit = (EditText) findViewById(R.id.password_edit);
		backBtn = (Button) findViewById(R.id.back_btn);
		loginBtn = (Button) findViewById(R.id.login_btn);
		
		initViewEvent();
	}

	private void initViewEvent() {
		backBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
		loginBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String id = idEdit.getText().toString();
				String password = passwordEdit.getText().toString();
				Cursor cursor = MainActivity.database.rawQuery("select * from "
						+ DatabaseHelper.TABLE_APP, null);
				cursor.moveToFirst();
				String idSQL = cursor.getString(cursor.getColumnIndex(DatabaseHelper.APP_USER_ID));
				String passwordSQL = cursor.getString(cursor.getColumnIndex(DatabaseHelper.APP_USER_PASSWORD));
				if (id.isEmpty()) {
					Toast.makeText(getApplicationContext(), "«Î ‰»Î’À∫≈", 
							Toast.LENGTH_SHORT).show();
				} else if(password.isEmpty()) {
					Toast.makeText(getApplicationContext(), "«Î ‰»Î√‹¬Î", 
							Toast.LENGTH_SHORT).show();
				} else if(id.equals(idSQL) && password.equals(passwordSQL)) {
					Toast.makeText(getApplicationContext(), "µ«¬º≥…π¶", 
							Toast.LENGTH_SHORT).show();
					MainActivity.isLogin = true;
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, ShelfActivity.class);
					finish();
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), "’À∫≈ªÚ√‹¬Î¥ÌŒÛ£¨«Î÷ÿ–¬ ‰»Î", 
							Toast.LENGTH_SHORT).show();
					idEdit.setText("");
					passwordEdit.setText("");
				}
			}
			
		});
	}
}
