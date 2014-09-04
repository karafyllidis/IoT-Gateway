package com.ots.iot.gateway.mqtt;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MqttConnection {
	
	private final static Logger log = Logger.getLogger(MqttConnection.class);
	private MqttClient client;
	private String serverURI;
	private String clientId = "com.ots.iot.gateway";
	private int qos = 2;
	private boolean retained = false;
	private String topic;
	
	public boolean connect(String serverURI, String topic) {
		this.serverURI = serverURI;
		this.topic = topic;
		return connect(this.serverURI, this.topic, this.clientId, this.qos, this.retained);
	}
	
	public boolean connect(String serverURI, String topic, String clientId, int qos, boolean retained) {
		this.serverURI = serverURI;
		this.topic = topic;
		this.clientId = clientId;
		this.qos = qos;
		this.retained = retained;
		
		try {
			client = new MqttClient(this.serverURI, this.clientId);
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
	
	public boolean reconnect() {
		if (this.serverURI != null)
			return connect(this.serverURI, this.topic);
		
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
		mqttMsg.setQos(this.qos);
		mqttMsg.setRetained(this.retained);		
		try {
			log.info("Publishing message: "+mqttMsg);
			client.publish(this.topic, mqttMsg);		
		} catch (MqttPersistenceException e) {
			log.error(e.getMessage());			
		} catch (MqttException e) {
			log.error(e.getMessage());
		}
	}
		

}
