package com.ots.iot.gateway;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.PropertyConfigurator;

import com.ots.iot.gateway.mqtt.MqttConnection;
import com.ots.iot.gateway.xbee.BaudRate;
import com.ots.iot.gateway.xbee.XBeeConnection;
import com.ots.iot.gateway.xbee.XBeePacketListener;

public class Gateway {
		
	private static final String ttyUSB0 = "/dev/ttyUSB0";
	private static final String mqtt_server = "tcp://tasos.duckdns.org:1883";
	private BlockingQueue<String> queue = null;
	
	Gateway() {
		XBeeConnection xbee = new XBeeConnection();
		if (xbee.connect(BaudRate.RATE_115200, ttyUSB0)) {
			MqttConnection mqtt = new MqttConnection();
			if (mqtt.connect(mqtt_server)) {
				queue = new ArrayBlockingQueue<String>(2000);				
				xbee.addPacketListener(new XBeePacketListener(queue));
				new Thread(new QueueProcessor(mqtt, queue)).start();
			}
		}
		
		
	}
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		new Gateway();
	}
	
}
