package com.yuanyu.lightmessage;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;

public class IntroductionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_introduction);
	}
	
	public void done(View view) {
		finish();
	}
}
