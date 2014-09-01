package com.ots.iot.gateway;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ots.iot.gateway.mqtt.MqttConnection;
import com.ots.iot.gateway.xbee.BaudRate;
import com.ots.iot.gateway.xbee.XBeeConnection;
import com.ots.iot.gateway.xbee.XBeePacketListener;

public class Gateway {
		
	private final static Logger log = Logger.getLogger(Gateway.class);
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
		Properties properties = new Properties();
		try {
			properties.load(new FileReader("gateway.properties"));			
			for (Map.Entry<Object, Object> property : properties.entrySet()) {
				log.debug(property.getKey() + " = " + property.getValue());
			}
		} catch (FileNotFoundException e) {
			log.warn("Could not find gateway.properties file in program path");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		new Gateway();
	}
	
}
