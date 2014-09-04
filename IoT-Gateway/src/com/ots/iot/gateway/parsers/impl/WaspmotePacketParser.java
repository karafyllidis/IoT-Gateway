package com.ots.iot.gateway.parsers.impl;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.ots.iot.gateway.parsers.XbeePacketParser;
import com.ots.iot.gateway.parsers.impl.Sensor.VariableType;
import com.rapplogic.xbee.util.ByteUtils;

/**
 * 
 * @author tasos

*/
/* 
 * Binary header and payload structure.
 *
 *                         HEADER                         |                          PAYLOAD
 * | <=> | 0x00 | 0x03 | 0x74F94515 | NODE_001 | # | 0x00 | ID | Byte 1 | Byte 2 | ID | Byte 1 | Byte 2 | ID | Byte 1 | Byte 2 |
 * |  A  |  B   |  C   |     E      |     F    | D |   G  |     Sensor 1         |     Sensor 2         |    Sensor 3          |
 *
 * A → Start Delimiter [3 Bytes]: It is composed by three characters: “<=>”. This is a 3-Byte field and it is necessary to identify each
 * frame starting.
 * 
 * B → Frame Type Byte [1Byte]: This field is used to determine the frame type. There are two kind of frames: Binary and ASCII.
 * But it also defines the aim of the frame such event frames or alarm frames. This field will be explained in the following sections.
 * 
 * C → Number of Fields Byte [1Byte]: This field specifies the number of sensor fields sent in the frame. This helps to calculate
 * the frame length.
 * 
 * D → Separator [1Byte]: The ‘#’ character defines a separator and it is put between some fields which length is not specified. This
 * helps to parse the different fields in reception.
 * 
 * E → Serial ID [4Byte]: This is a 4-Byte field which identifies each Waspmote device uniquely. The serial ID is get from a specific
 * chip integrated in Waspmote that gives a different identifier to each Waspmote device. So, it is only readable and it can not be
 * modified. Note that the Serial ID is sent as a binary field too.
 * 
 * F → Waspmote ID [variable]: This is a string defined by the user which may identify each Waspmote inside the user’s network.
 * The field size is variable [from 0 to 16Bytes]. When the user do not want to give any identifier, the field remains empty indicated
 * by a unique ‘#’ character.
 * 
 * G → Frame sequence [1Byte]: This field indicates the number of sent frame. This counter is 8-bit, so it goes from 0 to 255. Each
 * time it reaches the maximum 255 is reset to 0. This sequence number is used in order to detect loss of frames.
 * 
 */
 
public class WaspmotePacketParser implements XbeePacketParser {

	private static final String DASH = "#";
	private final static Logger log = Logger.getLogger(WaspmotePacketParser.class);

	
	@Override
	public String parse(int[] data) {
				
		int frameType[] = Arrays.copyOfRange(data, 3, 4);
						
		if (FrameType.ASCII.inRange(frameType[0])) {
			return handleAsciiData(data);
		} else if (FrameType.BINARY.inRange(frameType[0])) {
			return handleBinaryData(data);
		}
		
		log.warn("Unknown Waspmote frame type: "+frameType);
		return "";
    	
	}

	private String handleBinaryData(int[] data) {
		
		log.debug("Handling BINARY packet data");
		
		StringBuilder message = new StringBuilder();
		
		// Frame type (0 = ascii, 1 = binary)
		int frameType[] = Arrays.copyOfRange(data, 3, 4);
		String frameTypeStr = String.valueOf(frameType[0]);
		
		// Start delimiter <->
		String startDelimiterStr = "";    	
    	int startDelimiter[] = Arrays.copyOfRange(data, 0, 3);
    	for (int i = 0; i < startDelimiter.length; i++) {
    		char ch = (char) startDelimiter[i];
    		startDelimiterStr += ch;
    	}
    	
    	// Number of sensor fields
    	int numberOfFields[] = Arrays.copyOfRange(data, 4, 5);
    	String numberOfFieldsStr = String.valueOf(numberOfFields[0]);
    	
    	// Variable length Serial Id
    	int serialId[] = Arrays.copyOfRange(data, 5, 9);	    	
    	String serialIdStr = ByteUtils.toBase16(serialId);
    	
    	// Waspmote id
    	String waspmoteIdStr = null;
    	int delimiterPos;
    	int separator[] = Arrays.copyOfRange(data, 9, 10);
    	if (separator[0] == 35) {
    		waspmoteIdStr = DASH;
    		delimiterPos = 10;
    	} else {
    		delimiterPos = Arrays.binarySearch(data, 9, 26, 35);
    		int waspModeId[] = Arrays.copyOfRange(data, 9, delimiterPos);
    		waspmoteIdStr = ByteUtils.toString(waspModeId);
    	}
    	
    	// Frame sequence
    	int frameSequence[] = Arrays.copyOfRange(data, delimiterPos+1, delimiterPos+2);
    	String frameSequenceStr = String.valueOf(frameSequence[0]);
    	
    	delimiterPos = delimiterPos + 2;
    	
    	message.append(startDelimiterStr);
    	message.append(DASH);
    	message.append(frameTypeStr);
    	message.append(DASH);
    	message.append(numberOfFieldsStr);
    	message.append(DASH);
    	message.append(serialIdStr);
    	message.append(DASH);
    	message.append(waspmoteIdStr);
    	message.append(DASH);
    	message.append(frameSequenceStr);
    	message.append(DASH);
    	
    	// TODO parse sensor data
    	// subarray of data array containing binary data payload
    	int sensorData[] = Arrays.copyOfRange(data, delimiterPos, data.length);
    	int startPos = 0;
    	for (int i = 0; i < numberOfFields[0]; i++) {
    		int sensorId = sensorData[startPos];
    		Sensor sensor = Sensor.getSensorType(sensorId);
    		if (sensor.isVariableWidth()) {
    			int lengthPos = startPos + 1; 					// the position of the length value
    			int lengthValue = sensorData[lengthPos]; 		// the actual length value
    			int dataStartPos = lengthPos + 1; 				// the data start position is after the length position
    			int dataEndPos = dataStartPos + lengthValue; 	// the data end position is the data start position plus the length value
    			int value[] = Arrays.copyOfRange(sensorData, dataStartPos, dataEndPos);
    			message.append(ByteUtils.toString(value));
    			message.append(DASH);
    			startPos = dataEndPos;
    		} else {
    			int lengthValue = sensor.getFieldSize();				// the number of bytes the value is encoded to    			
    			String values[] = new String[sensor.getNumOfFields()];	// the number of fields
    			for (int k = 0; k < sensor.getNumOfFields(); k++) {
    				int dataStartPos = startPos + 1;
    				int dataEndPos = dataStartPos + lengthValue;
    				int value[] = Arrays.copyOfRange(sensorData, dataStartPos, dataEndPos);    				
    				String strVal = "";
    				
    				if (sensor.getVariableType() == VariableType.FLOAT) {
    					float sensorVal = WaspFloatToFloat(value);
    					// 4 digits 2 precision points
    					strVal = String.format("%1$4.2f", sensorVal);    					
    				} else if (sensor.getVariableType() == VariableType.INT) {
    					int sensorVal = WaspIntToInt(value);
    					strVal = String.format("%1$f", sensorVal);
    				}
    				values[k] = strVal;
    				startPos = dataEndPos;
    			}    			
    			for (int j = 0; j < values.length; j++) {
    				if (j > 0)
    					message.append(values[j]+";");
    				message.append(values[j]);
    			}
    			message.append(DASH);
    			//startPos = startPos + 1;    			
    		}
    		    		
    		
    	}
    	
    	
		
    	return message.toString();		
	}
	
	public static int WaspIntToInt(int sensorValue[]) {
		// reversing order of bytes		
		int reversedValue[] = reverseArray(sensorValue);
		// to hex				
		String hex = hexArrayToString(reversedValue);
		int val = Integer.parseInt(hex, 16);
		return val;
	}
	
	public static float WaspFloatToFloat(int sensorValue[]) {
		
		// reversing order of bytes		
		int reversedValue[] = reverseArray(sensorValue);
		// to hex				
		String hex = hexArrayToString(reversedValue);
		
		int hexToInt = Integer.parseInt(hex, 16);	
		float floatValue = Float.intBitsToFloat(hexToInt);
		
		return floatValue;		
	}
	
	private static int[] reverseArray(int[] array) {		
		int j = array.length;
		int reversedArray[] = new int[j];    				
		for (int i = 0; i < array.length; i++) {
			j--;
			reversedArray[j] = array[i];
		}
		return reversedArray;
	}
	
	private static String hexArrayToString(int hexValues[]) {
		String hex = ByteUtils.toBase16(hexValues);
		hex = hex.replaceAll(",", "");
		hex = hex.replaceAll("0x", "");
		return hex;
	}

	private String handleAsciiData(int[] data) {
		log.debug("Handling ASCII packet data");
		
		StringBuilder message = new StringBuilder();
		
		// remove start delimiter string and framesequence, keep the others
    	String dataStr = ByteUtils.toString(data);
    	int firstDashPos = dataStr.indexOf(DASH);
    	int frameSeqStartPos = dataStr.indexOf(DASH, dataStr.indexOf(DASH, firstDashPos + 1) + 1);
    	int frameSeqEndPos = dataStr.indexOf(DASH, frameSeqStartPos + 1);
    	
    	String firstPart = dataStr.substring(firstDashPos, frameSeqStartPos);
    	String secondPart = dataStr.substring(frameSeqEndPos);
    	message.append(firstPart);
    	message.append(secondPart);
    	
		return message.toString();
	}

}
