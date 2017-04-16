package readApp.view;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class BagrrageView extends TextView {
	private ViewGroup parent;
	private String bagrrage;
	private int duration;
	
	private TranslateAnimation anim;

	public BagrrageView(Context context, String bagrrage, int duration, ViewGroup parent) {
		super(context);
		this.bagrrage = bagrrage;
		this.duration = duration;
		this.parent = parent;
		this.setText(bagrrage);
		initAnim();
		initStyle();
	}

	private void initAnim() {
		float width = this.getPaint().measureText(bagrrage);
		float start = parent.getWidth();
		float end = -width;
		anim = new TranslateAnimation(start, end, 0, 0);
		anim.setDuration(duration);
		this.setAnimation(anim);
		
		anim.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationStart(Animation animation) {
				
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				parent.removeView(BagrrageView.this);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
		});
	}
	
	private void initStyle() {
		this.setTextColor(Color.argb(225, 0, 0, 0));
	}
	
	public void start(){
		this.startAnimation(anim);
	}
}
