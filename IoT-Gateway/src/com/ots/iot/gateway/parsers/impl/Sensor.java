package com.ots.iot.gateway.parsers.impl;

public enum Sensor {	
	
	Temperature_Celsius(12, 1, VariableType.FLOAT, "C"),
	Luminosity(22, 1, VariableType.FLOAT, "Ohms"),
	Presence(23, 1, VariableType.UINT8_T, ""),	
	Battery(52, 1, VariableType.UINT8_T, "%"),
	MAC_Address(55, 1, VariableType.STRING, ""),
	Internal_Temperature(62, 1, VariableType.FLOAT, "C"),
	Accelerometer(63, 3, VariableType.INT, "mg"),
	Sensor_String(65, 1, VariableType.STRING, "");

	private int sensor_id;
	private int numOfFields;
	private int variableType;
	private int fieldSize;
	private String unit;

	Sensor(int sensor_id, int numOfFields, int variableType, String unit) {
		this.sensor_id = sensor_id;
		this.numOfFields = numOfFields;
		this.variableType = variableType;
		this.fieldSize = variableType;
		this.unit = unit;
	}
			
	public int getSensorId() {
		return sensor_id;
	}
	
	public int getNumOfFields() {
		return numOfFields;
	}

	public int getVariableType() {
		return variableType;
	}

	public int getFieldSize() {
		return fieldSize;
	}

	public String getUnit() {
		return unit;
	}

	public static Sensor getSensorType(int id) {
		switch(id) {
			case 12: return Temperature_Celsius;	
			case 22: return Luminosity;
			case 23: return Presence;
			case 52: return Battery;
			case 55: return MAC_Address;
			case 62: return Internal_Temperature;
			case 63: return Accelerometer;
			case 65: return Sensor_String;
		}
		return null;	
	}
	
	public boolean isVariableWidth() {
		if (variableType == VariableType.STRING)
			return true;
		
		return false;
	}
	
	class VariableType {
		public static final int UINT8_T = 1;
		public static final int INT = 2;
		public static final int FLOAT = 4;
		public static final int ULONG = 4;
		public static final int STRING = -1;
	}
}
