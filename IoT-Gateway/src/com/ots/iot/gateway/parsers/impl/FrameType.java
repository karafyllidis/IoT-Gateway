package com.ots.iot.gateway.parsers.impl;

public enum FrameType {
	
	BINARY(0, 127),
	ASCII(128, 255);
	
	private int lower;
	private int upper;	

	FrameType(int lower, int upper) {
		this.lower = lower;
		this.upper = upper;
	}
	
	public boolean inRange(int type) {
		if (lower <= type && type <= upper)
			return true;
		
		return false;
	}

}
