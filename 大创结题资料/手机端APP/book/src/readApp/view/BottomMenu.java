package readApp.view;

import readApp.activity.R;
import readApp.activity.ReadActivity;
import readApp.activity.SetActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

public class BottomMenu extends LinearLayout {
	private Button bagrrageBtn;
	private Button setBtn;
	private ReadActivity activity;
	
	private Animation showAnim;
	private Animation closeAnim;

	public BottomMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (ReadActivity) this.getContext();

		this.setVisibility(View.INVISIBLE);
		initViews();
	}
	
	@SuppressLint("NewApi")
	private void initViews() {
		bagrrageBtn = new Button(this.getContext());
		bagrrageBtn.setBackground(getContext().getDrawable(R.drawable.white_btn));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(40, 40, 40, 40);
		bagrrageBtn.setLayoutParams(params);
		this.addView(bagrrageBtn);
		
		setBtn = new Button(this.getContext());
		setBtn.setBackground(getContext().getDrawable(R.drawable.white_btn));
		setBtn.setLayoutParams(params);
		setBtn.setText("…Ë    ÷√");
		this.addView(setBtn);
		
		initViewsEvent();
		initAnim();
	}

	private void initViewsEvent() {
		bagrrageBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(activity.isBagrrageOpen()) {
					activity.closeBagrrage();
				} else {
					activity.openBagrrage();
				}
			}
			
		});
		
		setBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getContext(), SetActivity.class);
				getContext().startActivity(intent);
			}
			
		});
	}
	
	private void initAnim(){
		showAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.show_bottom_anim);
		closeAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.close_bottom_anim);
		
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
	
	public void show() {
		this.setVisibility(View.VISIBLE);
		startAnimation(showAnim);
	}
	
	public void close() {
		this.setVisibility(View.INVISIBLE);
		startAnimation(closeAnim);
	}

	public Button getBagrrageBtn() {
		return bagrrageBtn;
	}

	public Button getSetBtn() {
		return setBtn;
	}
}
