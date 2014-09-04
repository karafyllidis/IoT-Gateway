package com.ots.iot.gateway;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.ots.iot.gateway.mqtt.MqttConnection;

public class QueueProcessor implements Runnable {
	
	private final static Logger log = Logger.getLogger(QueueProcessor.class);
	private BlockingQueue<String> queue;
	private MqttConnection mqtt;
		
	public QueueProcessor(MqttConnection mqtt, BlockingQueue<String> queue) {
		this.queue = queue;
		this.mqtt = mqtt;
	}

	@Override
	public void run() {
		log.info("Starting queue processor thread with initial capacity "+queue.remainingCapacity());	
		while (true) {			
			if (mqtt != null && mqtt.isConnected()) {
				String message;
				try {
					log.debug("Queue size: "+queue.size()+" ("+queue.remainingCapacity()+")");
					message = queue.take();
					if (message != null) {
						mqtt.sendMessage(message);
					}		
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			} else {
				log.warn("Gateway has lost connection with the Mqtt broker - will retry connection is 5s");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				mqtt.reconnect();
			}
		}
	}

}
