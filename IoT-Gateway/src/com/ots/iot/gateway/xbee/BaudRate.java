package com.ots.iot.gateway.xbee;

public enum BaudRate {
	
	RATE_9600(9600),
	RATE_19200(19200),
	RATE_38400(38400),
	RATE_57600(57600),
	RATE_115200(115200);
	
	private int rate;

	BaudRate(int rate) {
		this.rate = rate;
	}
	
	public int getValue() {
		return rate;
	}
}
