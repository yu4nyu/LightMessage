package com.yuanyu.lightmessage.utils;

public class AsciiBinaryTurn {
	public static boolean isAscii(String str) {
    	for(int i = 0; i < str.length(); i++) {
    		int n = str.charAt(i);
    		if(n > 127 || n < 0) {
    			return false;
    		}
    	}
    	
    	return true;
    }
	
	public boolean[] asciiToBool(String input) {
		int length = input.length();
    	boolean[] output = new boolean[length * 8];
    	
    	char c;
    	int numericNumber;
    	String binaryString;
    	for(int i = 0; i < length; i++) {
    		c = input.charAt(i);
    		numericNumber = c;
    		binaryString = Integer.toBinaryString(numericNumber);
    		for(int j = 0; j < 8; j++) {
    			if(j < 8 - binaryString.length()) {
    				output[i*8 + j] = false;
    			}
    			else {
    				if(binaryString.charAt(j + binaryString.length() - 8) == '1'){
    					output[i*8 + j] = true;
    				}
    				else{
    					output[i*8 + j] = false;
    				}
    			}
    		}
    	}
    	
    	return output;
    }
	
	public String boolToAscii(boolean[] input) {
		String output = new String("");
		int charNumber = input.length / 8; // 如果接受出错，丢弃无法整除的位
		String binaryString = null;
		int numericNumber;
		for(int i = 0; i < charNumber; i++) {
			binaryString = "";
			for(int j = 0; j < 8; j++) {
				if(input[output.length()*8 + j]) {
					binaryString += "1";
				}
				else {
					binaryString += "0";
				}
			}
			numericNumber = Integer.parseInt(binaryString, 2);
			output += (char)numericNumber;
		}
		
		return output;
	}
}
