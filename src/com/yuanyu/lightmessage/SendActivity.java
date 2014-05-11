package com.yuanyu.lightmessage;

import com.yuanyu.lightmessage.utils.AsciiBinaryTurn;
import com.yuanyu.lightmessage.utils.StrBinaryTurn;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SendActivity extends Activity {
	
	public static final int WHITE_MESSAGE = 1;
	public static final int BLACK_MESSAGE = 2;
	public static final int SEND_FINISHED = 3;
	public static final int COUNT_MESSAGE = 4;
	
	private TextView countdown;
	
	private boolean[] mBooleanMessage;
	private String mMessage;
	private RelativeLayout background;	
	private WindowManager.LayoutParams layoutParams;
	
	private boolean mCurrentStatus = true; // true means white, false means black
	
	private Thread mThread;
	
	@SuppressLint("HandlerLeak")
	private Handler mBlinkingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case WHITE_MESSAGE:
					background.setBackgroundColor(Color.WHITE);
					break;
				case BLACK_MESSAGE:
					background.setBackgroundColor(Color.BLACK);
					break;
				case SEND_FINISHED:
					setResult(Activity.RESULT_OK);
					finish();
					break;
				case COUNT_MESSAGE:
					int second = msg.getData().getInt("count");
					countdown.setText(second + "");
					if(second == 0) {
						countdown.setVisibility(View.INVISIBLE);
					}
					break;
			}
		}
	};
	
	private Runnable mBlinkingRunnable = new Runnable() {
		@Override
		public void run() {
			
			// Wait to send
			if(mCurrentStatus) {
				mBlinkingHandler.sendEmptyMessage(WHITE_MESSAGE);
			}
			else {
				mBlinkingHandler.sendEmptyMessage(BLACK_MESSAGE);
			}	
			countdown();
			
			// Send messages to indicate the start
			startOrEndFlag();
			
			// Send test message
			testMessage();
			
			// Send divider message
			if(mCurrentStatus) {
				mBlinkingHandler.sendEmptyMessage(BLACK_MESSAGE);
			}
			else {
				mBlinkingHandler.sendEmptyMessage(WHITE_MESSAGE);
			}
			mCurrentStatus = !mCurrentStatus;
			startOrEndFlag();
			
			// Start to send message
			for(int i = 0; i < mBooleanMessage.length; i++){
				if(mCurrentStatus) {
					mBlinkingHandler.sendEmptyMessage(BLACK_MESSAGE);
					mCurrentStatus = false;
				}
				else {
					mBlinkingHandler.sendEmptyMessage(WHITE_MESSAGE);
					mCurrentStatus = true;
				}
				
				if(mBooleanMessage[i]) {
					keepLongStatus();
				}
				else {
					keepShortStatus();
				}
				
				if(Thread.currentThread().isInterrupted()) {
					break;
				}
			}
			
			// Send messages to indicate the end
			if(mCurrentStatus) {
				mBlinkingHandler.sendEmptyMessage(BLACK_MESSAGE);
				mCurrentStatus = false;
			}
			else {
				mBlinkingHandler.sendEmptyMessage(WHITE_MESSAGE);
				mCurrentStatus = true;
			}
			startOrEndFlag();
			if(mCurrentStatus) {
				mBlinkingHandler.sendEmptyMessage(BLACK_MESSAGE);
			}
			else {
				mBlinkingHandler.sendEmptyMessage(WHITE_MESSAGE);
			}
			
			// Send finished
			keepLongStatus();
			mBlinkingHandler.sendEmptyMessage(SEND_FINISHED);
		}
		
		private void countdown() {
			int second = Constant.START_WAITING / 1000;
			int rest = Constant.START_WAITING % 1000;
			
			for(int i = 0; i < second; i++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Message message = mBlinkingHandler.obtainMessage(COUNT_MESSAGE);
				Bundle bundle = new Bundle();
				bundle.putInt("count", second - i - 1);
				message.setData(bundle);
				mBlinkingHandler.sendMessage(message);
			}
			
			try {
				Thread.sleep(rest);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void testMessage() {
			for(int i = 0; i < 6; i++) {
				if(mCurrentStatus) {
					mBlinkingHandler.sendEmptyMessage(BLACK_MESSAGE);
					mCurrentStatus = !mCurrentStatus;
				}
				else {
					mBlinkingHandler.sendEmptyMessage(WHITE_MESSAGE);
					mCurrentStatus = !mCurrentStatus;
				}
				
				if(i%2 == 0) {
					keepLongStatus();
				}
				else if(i%2 == 1) {
					keepShortStatus();
				}
			}
		}
		
		private void startOrEndFlag() {
			try {
				Thread.sleep(Constant.START_END_FLAG);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void keepLongStatus() {
			try {
				Thread.sleep(Constant.INTERVAL_LONG);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void keepShortStatus() {
			try {
				Thread.sleep(Constant.INTERVAL_SHORT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
		setContentView(R.layout.activity_send);
		
		layoutParams = getWindow().getAttributes();
		background = (RelativeLayout) findViewById(R.id.send_activity_background);
		countdown = (TextView) findViewById(R.id.send_activity_text);
		countdown.setText((Constant.START_WAITING / 1000) + "");
		
		Intent intent = getIntent();
		mMessage = intent.getStringExtra("message");
		mMessage = mMessage.trim(); // Delete useless space
		
		if(AsciiBinaryTurn.isAscii(mMessage)) {
			mBooleanMessage = strToAscii(mMessage);
		}
		else{
			mBooleanMessage = strToBool(mMessage);
		}
		
		mCurrentStatus = !mBooleanMessage[0];
		
		layoutParams.screenBrightness = 1.0f;
	    getWindow().setAttributes(layoutParams);
	    mThread = new Thread(mBlinkingRunnable);
	    mThread.start();
	}
	
	protected void onDestroy() {
		mBlinkingHandler.removeCallbacksAndMessages(null);
		mThread.interrupt();
		super.onDestroy();
	}
	
	// Convert unicode string to boolean array
    private boolean[] strToBool(String input) {
    	StrBinaryTurn turn = new StrBinaryTurn();
    	boolean[] turned = turn.StrToBool(input);
    	boolean[] output = new boolean[turned.length + 1];
    	output[0] = true;
    	for(int i = 0; i < turned.length; i++) {
    		output[i + 1] = turned[i];
    	}
    	return output;
    }
    
    private boolean[] strToAscii(String input) {
    	AsciiBinaryTurn turn = new AsciiBinaryTurn();
    	boolean[] turned = turn.asciiToBool(input);
    	boolean[] output = new boolean[turned.length + 1];
    	output[0] = false;
    	for(int i = 0; i < turned.length; i++) {
    		output[i + 1] = turned[i];
    	}
    	return output;
    }
}
