package com.ots.iot.gateway.xbee;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeConfiguration;
import com.rapplogic.xbee.api.XBeeException;

public class XBeeConnection {
	
	private final static Logger log = Logger.getLogger(XBeeConnection.class);
	private XBee xbee;	
	
	public boolean connect(BaudRate rate, String port) {
		xbee = new XBee(new XBeeConfiguration().withMaxQueueSize(100).withStartupChecks(false));		
		try {
			xbee.open(port, rate.getValue());
		} catch (XBeeException e) {			
			log.error(e.getMessage());
			return false;
		}
		if (xbee.isConnected()) {
			log.info("Connected to Xbee radio at port "+port);
			return true;
		}		
		return false;
	}
	
	public void disconnect() {
		if (xbee != null)
			xbee.close();
	}
	
	public void addPacketListener(PacketListener listener) {
		if (xbee != null && listener != null)
			xbee.addPacketListener(listener);
				
	}
	
	public void removePacketListener(PacketListener listener) {
		if (xbee != null && listener != null)
			xbee.removePacketListener(listener);
	}
	

}
