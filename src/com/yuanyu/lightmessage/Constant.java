package com.yuanyu.lightmessage;

public class Constant {
	
	public static final int START_WAITING = 5000;
	public static final int START_END_FLAG = 1000;
	public static final int INTERVAL_SHORT = 200;
	public static final int INTERVAL_LONG = 450;
	public static final int DEVIATION = 150;
	public static final int RECEIVE_FLAG_THRESHOLD = 800;
	public static final int RECEIVE_FREQUENCY = 200;
	public static final String AD_ID = "ca-app-pub-3028123579469785/6714534953";
	
	public static final int TEST_TIMEOUT = 10000;
	
	public static enum SendStyle {BY_SCREEN, BY_LED}
	
	public static enum ReceiveStyle {BY_SENSOR, BY_CAMERA}
}