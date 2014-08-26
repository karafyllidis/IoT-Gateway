package com.ots.iot.gateway.xbee;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.util.ByteUtils;

public class XBeePacketListener implements PacketListener {

	private final static Logger log = Logger.getLogger(XBeePacketListener.class);
	private BlockingQueue<String> queue;

	public XBeePacketListener(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	@Override
	public void processResponse(XBeeResponse response) {
		RxResponse rxResponse = (RxResponse) response;	    
	    int data[] = rxResponse.getData();				    
	    if (data != null) {				    	
	    	String message = (ByteUtils.toString(data));
	    	log.debug("Handling message: "+message);
	    	try {
				queue.offer(message, 10, TimeUnit.SECONDS);				
			} catch (InterruptedException e) {
				log.warn("Could not put message into queue: "+e.getMessage());
			}
	    }
	}

}
