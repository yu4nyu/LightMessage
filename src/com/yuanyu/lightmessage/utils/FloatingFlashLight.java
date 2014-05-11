package com.yuanyu.lightmessage.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.yuanyu.lightmessage.R;

public enum FloatingFlashLight {
	
	INSTANCE;
	
	private WindowManager wm=null;
	private WindowManager.LayoutParams wmParams=null;
	
	private ImageView flashlight;
	
	private boolean isFlashOn = false;
	
	public void create(Context context) {
		initFloatView(context);
		createLeftFloatView(context);
	}
	
	public void destroy() {
		wm.removeView(flashlight);
		LedFlash.INSTANCE.release();
	}
	
	public void makeVisible() {
		if(null != flashlight) {
			flashlight.setVisibility(View.VISIBLE);
		}
	}
	
	public void makeInvisible() {
		if(null != flashlight) {
			flashlight.setVisibility(View.INVISIBLE);
		}
	}

	private void initFloatView(Context context){
	    wm = (WindowManager)context.getApplicationContext().getSystemService("window");
	    DisplayMetrics display = new DisplayMetrics();
	    wm.getDefaultDisplay().getMetrics(display);
	    wmParams = new WindowManager.LayoutParams();
	    	
	    wmParams.type = LayoutParams.TYPE_PHONE;
	    wmParams.format = PixelFormat.RGBA_8888;
	    wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;

	    wmParams.x = 0;
	    wmParams.y = 0;
	    wmParams.width=64;
	    wmParams.height=64;
	}
	
    private void createLeftFloatView(Context context){
    	flashlight = new ImageView(context);
    	flashlight.setImageResource(R.drawable.flashlight_off);
		flashlight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isFlashOn) {
					flashlight.setImageResource(R.drawable.flashlight_off);
					LedFlash.INSTANCE.closeLightOff();
				}
				else {
					flashlight.setImageResource(R.drawable.flashlight_on);
					LedFlash.INSTANCE.openLightOn();
				}
				isFlashOn = !isFlashOn;
			}
    	});
        wmParams.gravity=Gravity.LEFT | Gravity.CENTER_VERTICAL;
        wm.addView(flashlight, wmParams);
    }
}
