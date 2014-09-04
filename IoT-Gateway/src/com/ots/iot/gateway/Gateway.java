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
	//private static final String ttyUSB0 = "/dev/ttyUSB0";
	//private static final String mqtt_server = "tcp://tasos.duckdns.org:1883";
	private BlockingQueue<String> queue = null;	
	
	Gateway(Properties properties) {
		
		XBeeConnection xbee = new XBeeConnection();		
		String baudRate = "RATE_"+properties.getProperty("xbee.baudrate");
		String port = properties.getProperty("xbee.serialport");
		
		if (baudRate == null || baudRate.isEmpty() || port == null || port.isEmpty()) {
			log.error("Specify serial connection baud rate and/or serial port where the xbee radio is connected to.");
		} else {		
			
			if (xbee.connect(BaudRate.valueOf(baudRate), port)) {				
				MqttConnection mqtt = new MqttConnection();				
				String mqttServer = properties.getProperty("mqtt.server");
				String topic = properties.getProperty("mqtt.topic");
				if (mqttServer == null || mqttServer.isEmpty() || topic == null || topic.isEmpty()) {
					log.error("Specify the Mqtt broker URI and/or topic.");
				} else {					
					if (mqtt.connect(mqttServer, topic)) {
						queue = new ArrayBlockingQueue<String>(2000);				
						xbee.addPacketListener(new XBeePacketListener(queue));
						new Thread(new QueueProcessor(mqtt, queue)).start();
					}
				}			
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
		new Gateway(properties);
	}
	
}
