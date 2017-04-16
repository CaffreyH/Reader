package readApp.activity;

import java.util.LinkedList;

import readApp.book.Book;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ShelfActivity extends Activity {
	private LinkedList<Book> booksList = new LinkedList<Book>();
	
	private GridView shelfView;
	private Button backBtn;
	private Button shopBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shelf);
		
		init();
	}
	
	private void init(){
		initView();
		booksList = MainActivity.booksList;
		showBooks();
	}
	
	private void initView(){
		shelfView = (GridView) findViewById(R.id.shelf_view);
		backBtn = (Button) findViewById(R.id.back_btn);
		shopBtn = (Button) findViewById(R.id.shop_btn);
		
		initViewEvent();
		
	}
	
	private void initViewEvent(){
		shelfView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Book currentBook = booksList.get(position);
				Intent intent = new Intent();
				intent.putExtra("bookId", currentBook.getBookId());
				intent.setClass(ShelfActivity.this, ReadActivity.class);
				startActivity(intent);
			}
			
		});
		
		backBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
		shopBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ShelfActivity.this, ShopActivity.class);
				startActivity(intent);
				finish();
			}
			
		});
	}
	
	private void showBooks(){
		ShelfAdapter adapter = new ShelfAdapter(booksList);
		shelfView.setAdapter(adapter);
	}
	
	private class ShelfAdapter extends BaseAdapter{
		private LinkedList<Book> booksList;
		
		public ShelfAdapter(LinkedList<Book> booksList){
			this.booksList = booksList;
		}

		@Override
		public int getCount() {
			return booksList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint({ "ViewHolder", "InflateParams" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout layout = (LinearLayout) LayoutInflater.from(ShelfActivity.this).inflate(R.layout.view_book, null);
			ImageView image = (ImageView) layout.getChildAt(0);
			image.setImageDrawable(booksList.get(position).getBookImage());
			return image;
		}
		
	}
}
