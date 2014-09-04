package com.ots.iot.gateway.xbee;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ots.iot.gateway.parsers.XbeePacketParser;
import com.ots.iot.gateway.parsers.impl.WaspmotePacketParser;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;

public class XBeePacketListener implements PacketListener {

	private final static Logger log = Logger.getLogger(XBeePacketListener.class);
	private BlockingQueue<String> queue;
	private XbeePacketParser waspmoteParser = new WaspmotePacketParser();

	public XBeePacketListener(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	@Override
	public void processResponse(XBeeResponse response) {
		RxResponse rxResponse = (RxResponse) response;	    
		
		/*if (response.getApiId() == ApiId.RX_16_RESPONSE) {
			log.info("Received RX 16 packet " + ((RxResponse16)response));
		} else if (response.getApiId() == ApiId.RX_64_RESPONSE) {
			log.info("Received RX 64 packet " + ((RxResponse64)response));
			log.info("64 msg = " + ByteUtils.toString(((RxResponse64)response).getData()));
		}*/
		
		int data[] = rxResponse.getData();
		
	    if (data != null) {	    	
	    	String message = waspmoteParser.parse(data); 
	    	if (message != null) {
		    	log.debug("Pushing message into queue: "+message);
		    	try {
					queue.offer(message, 10, TimeUnit.SECONDS);				
				} catch (InterruptedException e) {
					log.warn("Could not put message into queue: "+e.getMessage());
				}
	    	} else {
	    		log.warn("Skipping message: "+data);
	    	}
	    }
	}

}
