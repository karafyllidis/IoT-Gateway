package com.ots.iot.gateway.mqtt;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttListener implements MqttCallback {
		
	private final static Logger log = Logger.getLogger(MqttListener.class);

	@Override
	public void connectionLost(Throwable cause) {
		log.error("Connection to broker has been lost: "+cause.getMessage());		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}

}
