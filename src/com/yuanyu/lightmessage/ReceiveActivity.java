package com.yuanyu.lightmessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.yuanyu.lightmessage.utils.AsciiBinaryTurn;
import com.yuanyu.lightmessage.utils.StrBinaryTurn;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class ReceiveActivity extends Activity implements SensorEventListener {
	
	private static final int WHITE = 0;
	private static final int BLACK = 1;
	private static final int GRAY = 2;

	private SensorManager sensorManager;  
	private Sensor lightSensor;
	
	private RelativeLayout background;
	private ProgressBar progress;
	
	private Map<Long, Float> valueList;
	private boolean isStart = false;
	private long startPosition = 0;
	private long endPosition = 0;
	
	private long lastValueTime = 0;
	private long currentValueTime = 0;
	
	private Map<Long, Float> testValues;
	private int startSymbol = 0; // 0 means not start, 1 means start to receive test message, 2 means start
	
	private float mMax = Float.MIN_VALUE;
	private float mMin = Float.MAX_VALUE;
	
	private Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// No title and full screen
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_receive);
		
		background = (RelativeLayout)findViewById(R.id.receive_activity_background);
		background.setBackgroundColor(Color.BLACK);
		progress = (ProgressBar)findViewById(R.id.receive_activity_progress);
		
		valueList = new TreeMap<Long, Float>();
		testValues = new TreeMap<Long, Float>();
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if(lightSensor == null || lightSensor.getMinDelay() > Constant.RECEIVE_FREQUENCY) {
			showNotCompatibleDialog();
		}
		
		mHandler = new Handler();
		mHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				if(startSymbol != 2) {
					showReceiveFailedDialog();
					progress.setVisibility(View.INVISIBLE);
					sensorManager.unregisterListener(ReceiveActivity.this);
				}
			}
		}, Constant.TEST_TIMEOUT);
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		sensorManager.registerListener(this, lightSensor, Constant.RECEIVE_FREQUENCY);
	}
	
	@Override
	protected void onPause() {
		sensorManager.unregisterListener(this);
		mHandler.removeCallbacksAndMessages(null);
		super.onPause();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float lux = event.values[0];
		Log.d("YY", "Light value " + lux);
		if(startSymbol == 2 && recognize(lux) == GRAY ) {
			return;
		}
		
		lastValueTime = currentValueTime;
		currentValueTime = event.timestamp / 1000000;
		
		long interval = currentValueTime - lastValueTime;
		if(interval > Constant.RECEIVE_FLAG_THRESHOLD && lastValueTime != 0) {
			startSymbol++;
			Log.d("YY", "start symbol " + startSymbol);
		}
		
		if(startSymbol < 2) { // Receive value as test messages
			testValues.put(currentValueTime, lux);
			if(lux > mMax) {
				mMax = lux;
			}
			if(lux < mMin) {
				mMin = lux;
			}
			return;
		}
		
		if(!isEnded(lux)){
			
			valueList.put(currentValueTime, lux);
			if(!isStart && startSymbol == 2) {
				isStart = true;
				analyseTestMessage();
				startPosition = currentValueTime;
			}
		}
		else {
			sensorManager.unregisterListener(this);
			Log.d("End", "Receive ended");
			analyze();
		}
	}
	
	private void analyseTestMessage() {
		int maxNumber = 0, minNumber = 0;
		for(Map.Entry<Long, Float> entry : testValues.entrySet()) {
			if(entry.getValue() == mMax) {
				maxNumber++;
			}
			if(entry.getValue() == mMin) {
				minNumber++;
			}
		}
		if(maxNumber != 4 || minNumber != 4) {
			showReceiveFailedDialog();
			progress.setVisibility(View.INVISIBLE);
			sensorManager.unregisterListener(this);
			mHandler.removeCallbacksAndMessages(null);
		}
	}
	
	private int recognize(float value) {
		if(value == mMin) {
			Log.d("Message", "black");
			return BLACK;
		}
		if(value == mMax) {
			Log.d("Message", "white");
			return WHITE;
		}
		Log.d("Message", "gray");
		return GRAY;
	}
	
	private boolean isEnded(float value) {
		if(!isStart){
			return false;
		}
		
		if(currentValueTime - lastValueTime > Constant.RECEIVE_FLAG_THRESHOLD) {
			endPosition = lastValueTime;
			return true;
		}
		
		return false;
	}
	
	private long hasValueBeforeStart(Map<Long, Integer> map) { // return 0 is not, else return the first found key
		for(Map.Entry<Long, Integer> entry : map.entrySet()) {
			if(entry.getKey() < startPosition) {
				return entry.getKey();
			}
		}
		return 0;
	}
	
	private long hasValueAfterEnd(Map<Long, Integer> map) { // return 0 is not, else return the first found key
		for(Map.Entry<Long, Integer> entry : map.entrySet()) {
			if(entry.getKey() > endPosition) {
				return entry.getKey();
			}
		}
		return 0;
	}
	
	private void removeRedundant(Map<Long, Integer> map) {
		// Delete values before start
		long toRemove = hasValueBeforeStart(map);
		while(toRemove != 0) {
			map.remove(toRemove);
			toRemove = hasValueBeforeStart(map);
		}
		
		// Remove end flag value
		toRemove = hasValueAfterEnd(map);
		while(toRemove != 0) {
			map.remove(toRemove);
			toRemove = hasValueAfterEnd(map);
		}
	}
	
	private boolean decodeTimeDifference(long first, long second) {
		int difference = (int) (second - first);
		
		/*int min = Constant.INTERVAL_SHORT - Constant.DEVIATION;
		if(min < 0) {
			min = 0;
		}
		int max = Constant.INTERVAL_SHORT + Constant.DEVIATION;
		if(difference >= min && difference <= max) {
			return false;
		}*/
		
		// Maybe use this to make it more precise
		/*min = Constant.INTERVAL_LONG - Constant.DEVIATION;
		max = Constant.INTERVAL_LONG + Constant.DEVIATION;
		if(difference >= min && difference <= max) {
			return false;
		}*/
		
		int middleValue = (Constant.INTERVAL_LONG + Constant.INTERVAL_SHORT) / 2;
		if(difference < middleValue) {
			return false;
		}
		
		return true;
	}
	
	private void analyze() {
		
		Map<Long, Integer> map = new TreeMap<Long, Integer>(); // TODO, use just a sorted list
		// Recognize all data
		for(Map.Entry<Long, Float> entry : valueList.entrySet()) {
			map.put(entry.getKey(), recognize(entry.getValue()));
		}
		
		removeRedundant(map);	
		
		// Decoding
		List<Boolean> list = new ArrayList<Boolean>();
		long lastKey = 0;
		for(Map.Entry<Long, Integer> entry : map.entrySet()) {
			if(lastKey == 0) {
				lastKey = entry.getKey();
				continue;
			}
			list.add(decodeTimeDifference(lastKey, entry.getKey()));
			lastKey = entry.getKey();
		}
		
		boolean[] array = new boolean[list.size()];
		for(int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		
		if(array.length == 0) {
			showReceiveFailedDialog();
			return;
		}
		
		boolean isUnicode = array[0];
		boolean[] newArray = new boolean[array.length - 1];
		for(int i = 0; i < array.length - 1; i++) {
			newArray[i] = array[i + 1];
		}
		
		if(array.length == 0) {
			this.setResult(MainActivity.RESULT_EMPTY_RESULT);
			finish();
		}
		
		String result = "";
		if(isUnicode) { // Unicode
			StrBinaryTurn turn = new StrBinaryTurn();
			result = turn.BoolToStr(newArray);
		}
		else { // Ascii
			AsciiBinaryTurn turn = new AsciiBinaryTurn();
			result = turn.boolToAscii(newArray);
		}
		
		Intent intent = new Intent();
		intent.putExtra("message", result);
		this.setResult(Activity.RESULT_OK, intent);
		finish();
	}
	
	private void showNotCompatibleDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.not_compatible_title)
			.setMessage(R.string.not_compatible_content)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.show();
	}
	
	private void showReceiveFailedDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.receive_fail_title)
			.setMessage(R.string.receive_fail_content)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.show();
	}
}
