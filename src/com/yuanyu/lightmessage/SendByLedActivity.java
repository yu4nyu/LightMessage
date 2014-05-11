package com.yuanyu.lightmessage;

import com.yuanyu.lightmessage.utils.AsciiBinaryTurn;
import com.yuanyu.lightmessage.utils.LedFlash;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SendByLedActivity extends Activity {
	
	private static boolean THREAD_RUNNING = true;
	
	public static final int WHITE_MESSAGE = 1;
	public static final int BLACK_MESSAGE = 2;
	public static final int SEND_FINISHED = 3;
	public static final int COUNT_MESSAGE = 4;
	public static final int PROGRESS = 5;
	
	private TextView mCountdown;
	private ProgressBar mProgress;
	
	private boolean[] mBooleanMessage;
	private String mMessage;
	private RelativeLayout background;	
	
	private boolean mCurrentStatus = true; // true means white, false means black
	
	private Thread mThread;
	
	@SuppressLint("HandlerLeak")
	private Handler mBlinkingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case WHITE_MESSAGE:
					LedFlash.INSTANCE.openLightOn();
					break;
				case BLACK_MESSAGE:
					LedFlash.INSTANCE.closeLightOff();
					break;
				case SEND_FINISHED:
					setResult(Activity.RESULT_OK);
					finish();
					break;
				case COUNT_MESSAGE:
					int second = msg.getData().getInt("count");
					mCountdown.setText(second + "");
					if(second == 0) {
						mCountdown.setVisibility(View.INVISIBLE);
					}
					break;
				case PROGRESS:
					int progress = mProgress.getProgress();
					if(progress == 0) {
						mProgress.setVisibility(View.VISIBLE);
					}
					mProgress.setProgress(progress + 1);
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
			
			if(!THREAD_RUNNING) {
				LedFlash.INSTANCE.release();
				return;
			}
			
			// Send messages to indicate the start
			startOrEndFlag();

			if(!THREAD_RUNNING) {
				LedFlash.INSTANCE.release();
				return;
			}
			
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
			
			if(!THREAD_RUNNING) {
				LedFlash.INSTANCE.release();
				return;
			}
			
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
				
				mBlinkingHandler.sendEmptyMessage(PROGRESS);
				
				if(!THREAD_RUNNING) {
					LedFlash.INSTANCE.release();
					return;
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
			
			if(!THREAD_RUNNING) {
				LedFlash.INSTANCE.release();
				return;
			}
			
			if(mCurrentStatus) {
				mBlinkingHandler.sendEmptyMessage(BLACK_MESSAGE);
			}
			else {
				mBlinkingHandler.sendEmptyMessage(WHITE_MESSAGE);
			}
			
			if(!THREAD_RUNNING) {
				LedFlash.INSTANCE.release();
				return;
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
				
				if(!THREAD_RUNNING) {
					LedFlash.INSTANCE.release();
					return;
				}
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
				
				mBlinkingHandler.sendEmptyMessage(PROGRESS);
				
				if(!THREAD_RUNNING) {
					LedFlash.INSTANCE.release();
					return;
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
		
		background = (RelativeLayout) findViewById(R.id.send_activity_background);
		background.setBackgroundColor(Color.BLACK);
		mCountdown = (TextView) findViewById(R.id.send_activity_text);
		mCountdown.setText((Constant.START_WAITING / 1000) + "");
		mProgress = (ProgressBar) findViewById(R.id.send_activity_progress);
		
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
		mProgress.setMax(mBooleanMessage.length + 6);
		mProgress.setProgress(0);
		
	    THREAD_RUNNING = true;
		mThread = new Thread(mBlinkingRunnable);
		mThread.start();
	}
	
	@Override
	protected void onDestroy() {
		THREAD_RUNNING = false;
		mBlinkingHandler.removeCallbacksAndMessages(null);
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
