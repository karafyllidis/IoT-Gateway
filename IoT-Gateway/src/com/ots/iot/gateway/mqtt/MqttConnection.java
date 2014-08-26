package com.ots.iot.gateway.mqtt;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MqttConnection {
	
	private static final String MQTT_CLIENT_ID = "com.ots.iot.gateway";
	private final static Logger log = Logger.getLogger(MqttConnection.class);
	private MqttClient client;
	
	public boolean connect(String serverURI) {
		try {
			client = new MqttClient(serverURI, MQTT_CLIENT_ID);
			client.connect();
		} catch (MqttException e) {
			log.error(e.getMessage());
		}
		
		if (client.isConnected()) {
			log.info("Connected to Mqtt server "+serverURI);
			return true;
		}				
		return false;
	}
	
	public void disconnect() {
		if (client != null) {
			try {
				client.disconnect();
			} catch (MqttException e) {
				log.warn(e.getMessage());
			}
		}
	}
	
	public void addListener(MqttCallback listener) {
		if (client != null)
			client.setCallback(listener);		
	}
	
	public boolean isConnected() {
		if (client != null && client.isConnected())
			return true;
		
		return false;
	}

	public void sendMessage(String message) {		
		MqttMessage mqttMsg = new MqttMessage(message.getBytes());
		mqttMsg.setQos(2);
		mqttMsg.setRetained(false);		
		try {
			log.info("Publishing message: "+mqttMsg);
			client.publish("waspmote_1", mqttMsg);		
		} catch (MqttPersistenceException e) {
			log.error(e.getMessage());			
		} catch (MqttException e) {
			log.error(e.getMessage());
		}
	}
		

}
