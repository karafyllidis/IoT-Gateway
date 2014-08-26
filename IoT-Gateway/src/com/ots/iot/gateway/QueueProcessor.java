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
		while(true) {
			log.debug("Queue size: "+queue.size()+" ("+queue.remainingCapacity()+")");
			String message;
			try {
				message = queue.take();
				if (message != null && mqtt != null && mqtt.isConnected()) {
					mqtt.sendMessage(message);
				}		
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			
				
		}
	}

}
