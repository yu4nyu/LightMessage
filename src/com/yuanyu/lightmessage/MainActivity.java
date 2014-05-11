package com.yuanyu.lightmessage;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.ads.*;
import com.yuanyu.lightmessage.Constant.ReceiveStyle;
import com.yuanyu.lightmessage.Constant.SendStyle;
import com.yuanyu.lightmessage.utils.FloatingFlashLight;
import com.yuanyu.lightmessage.utils.LightAnimation;

public class MainActivity extends Activity {
	
	public final static int RESULT_EMPTY_RESULT = Activity.RESULT_FIRST_USER;
	
	private final static int ACTIVITY_SEND = 0;
	private final static int ACTIVITY_RECEIVE = 1;
	private final static int ACTIVITY_INTRO = 2;

	EditText editText;
	LinearLayout linearLayout;
	AdView adView;
	
	ToggleButton mSendByScreen;
	ToggleButton mSendByLed;
	ToggleButton mReceiveBySensor;
	ToggleButton mReceiveByCamera;
	ImageView mLightningImage;
	
	SendStyle mSendStyle = SendStyle.BY_SCREEN;
	ReceiveStyle mReceiveStyle = ReceiveStyle.BY_SENSOR;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		editText = (EditText) findViewById(R.id.edit_text);
		linearLayout = (LinearLayout) findViewById(R.id.main_activity_layout);
		mLightningImage = (ImageView) findViewById(R.id.main_activity_lightning_image);
		
		adView = new AdView(this);
		adView.setAdUnitId(Constant.AD_ID);
		adView.setAdSize(AdSize.BANNER);
		linearLayout.addView(adView, 0);
		AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
	    
	    initToggleButtons();
	    FloatingFlashLight.INSTANCE.create(this);
	}

	public void send(View view) {
		String message = editText.getText().toString();
		if(message.equals("") || message.trim().equals("")) {
			editText.setError(getString(R.string.empty_message_error));
			return;
		}
		
		Intent intent = null;
		if(mSendStyle == SendStyle.BY_LED) {
			intent	= new Intent(MainActivity.this, SendByLedActivity.class);
		}
		else {
			intent	= new Intent(MainActivity.this, SendActivity.class);
		}
		intent.putExtra("message", message);
		startActivityForResult(intent, ACTIVITY_SEND);
		FloatingFlashLight.INSTANCE.makeInvisible();
	}

	public void receive(View view) {
		Intent intent = null;
		if(mReceiveStyle == ReceiveStyle.BY_SENSOR) {
			intent	= new Intent(MainActivity.this, ReceiveActivity.class);
		}
		else {
			//intent	= new Intent(MainActivity.this, ReceiveActivity.class);
			Toast.makeText(this, R.string.not_yet_implemented, Toast.LENGTH_SHORT).show();
			return;
		}
		startActivityForResult(intent, ACTIVITY_RECEIVE);
		FloatingFlashLight.INSTANCE.makeInvisible();
	}
	
	public void introduction(View view) {
		Intent intent = new Intent(MainActivity.this, IntroductionActivity.class);
		startActivityForResult(intent, ACTIVITY_INTRO);
		FloatingFlashLight.INSTANCE.makeInvisible();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case ACTIVITY_SEND:
				Toast.makeText(this, R.string.send_success, Toast.LENGTH_LONG).show();
				break;
			case ACTIVITY_RECEIVE:
				String result = data.getStringExtra("message");
				editText.setText(result);
				break;
			}
		}
		else if(resultCode == RESULT_EMPTY_RESULT) {
			
		}
		
		FloatingFlashLight.INSTANCE.makeVisible();
	}
	
	private void initToggleButtons() {
		mSendByScreen = (ToggleButton) findViewById(R.id.main_activity_send_by_screen);
	    mSendByLed = (ToggleButton) findViewById(R.id.main_activity_send_by_led);
	    mReceiveBySensor = (ToggleButton) findViewById(R.id.main_activity_receive_by_sensor);
	    mReceiveByCamera = (ToggleButton) findViewById(R.id.main_activity_receive_by_camera);
	    
	    mSendByScreen.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				mSendByScreen.setChecked(true);
				mSendByLed.setChecked(false);
				mSendStyle = SendStyle.BY_SCREEN;
				LightAnimation.INSTANCE.start(mLightningImage, mSendStyle, mReceiveStyle);
			}
	    });
	    mSendByLed.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				mSendByLed.setChecked(true);
				mSendByScreen.setChecked(false);
				mSendStyle = SendStyle.BY_LED;
				LightAnimation.INSTANCE.start(mLightningImage, mSendStyle, mReceiveStyle);
			}
	    });
	    mReceiveBySensor.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				mReceiveBySensor.setChecked(true);
				mReceiveByCamera.setChecked(false);
				mReceiveStyle = ReceiveStyle.BY_SENSOR;
				LightAnimation.INSTANCE.start(mLightningImage, mSendStyle, mReceiveStyle);
			}
	    });
	    mReceiveByCamera.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				mReceiveByCamera.setChecked(true);
				mReceiveBySensor.setChecked(false);
				mReceiveStyle = ReceiveStyle.BY_CAMERA;
				LightAnimation.INSTANCE.start(mLightningImage, mSendStyle, mReceiveStyle);
			}
	    });
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FloatingFlashLight.INSTANCE.destroy();
	}
}
