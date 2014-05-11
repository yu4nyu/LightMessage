package com.yuanyu.lightmessage.utils;

import java.util.Random;

import com.yuanyu.lightmessage.R;
import com.yuanyu.lightmessage.Constant.ReceiveStyle;
import com.yuanyu.lightmessage.Constant.SendStyle;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public enum LightAnimation {
	
	INSTANCE;
	
	public void start(ImageView view, SendStyle sendStyle, ReceiveStyle receiveStyle) {
		
		String imageFileName;
		Resources resources = view.getResources();
		Context context = view.getContext();
		switch(sendStyle) {
		case BY_SCREEN:
			switch(receiveStyle) {
			case BY_SENSOR:
				imageFileName = "light_left_to_left" + randomIndex();
				String PACKAGE_NAME = context.getPackageName();
				int imageResource = resources.getIdentifier(PACKAGE_NAME + ":drawable/" + imageFileName, null, null);
				view.setImageResource(imageResource);
				randomAnimation(view);
				break;
			case BY_CAMERA:
				view.setImageResource(R.drawable.light_left_to_right);
				randomAnimation(view);
				break;
			}
			break;
		case BY_LED:
			switch(receiveStyle) {
			case BY_SENSOR:
				view.setImageResource(R.drawable.light_right_to_left);
				randomAnimation(view);
				break;
			case BY_CAMERA:
				imageFileName = "light_right_to_right" + randomIndex();
				String PACKAGE_NAME = context.getPackageName();
				int imageResource = resources.getIdentifier(PACKAGE_NAME + ":drawable/" + imageFileName, null, null);
				view.setImageResource(imageResource);
				randomAnimation(view);
				break;
			}
			break;
		}
	}
	
	private int randomIndex() {
		Random random = new Random();
		return random.nextInt(3) + 1;
	}
	
	private int randomDuration() {
		Random random = new Random();
		return random.nextInt(300) + 100;
	}
	
	private void blink(final ImageView view, Handler handler) {
		view.setVisibility(View.VISIBLE);
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				view.setVisibility(View.INVISIBLE);
			}
		}, 50);
	}
	
	private void randomAnimation(final ImageView view) {
		final Handler handler = new Handler();
		blink(view, handler);
		
		int delay = 0;
		for(int i = 0; i < 3; i++) {
			delay += randomDuration();
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					blink(view, handler);
				}
			}, delay);
		}
		
	}
}
