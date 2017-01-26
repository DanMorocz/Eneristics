package org.eclipse.kura.example.ble.tisensortag;

public class SensorDocument {
	
	public static final String DEVICE_ID_MONGO_NAME = "device_id";
	public static final String DEVICE_TIME_MONGO_NAME = "timestamp";
	public static final String DEVICE_TEMP_MONGO_NAME = "ambient";
	public static final String DEVICE_HUMIDITY_MONGO_NAME = "humidity";
	
	// ***TO MODIFY TO MATCH YOUR EXISTING KURAPAYLOAD KEYS!!!****
	public static final String DEVICE_ID_KURA_NAME = "device_id";
	public static final String DEVICE_TIME_KURA_NAME = "timestamp";
	public static final String DEVICE_TEMP_KURA_NAME = "ambient";
	public static final String DEVICE_HUMIDITY_KURA_NAME = "humidity";

	private String device;
	private String timestamp;
	private String temperature;
	private String humidity;
	
	public SensorDocument() {
		device = null;
		timestamp = "0l";
		temperature = "0";
		humidity = "0";
	}
	
	public SensorDocument(String deviceName, String sampleTime, String tempReading, String humidityReading) {
		device = deviceName;
		timestamp = sampleTime;
		temperature = tempReading;
		humidity = humidityReading;
	}
	/**
	 * @return the device
	 */
	public String getDevice() {
		return device;
	}
	/**
	 * @param device the device to set
	 */
	public void setDevice(String device) {
		this.device = device;
	}
	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}
	/**
	 * @param string the timestamp to set
	 */
	public void setTimestamp(String string) {
		this.timestamp = string;
	}
	/**
	 * @return the temperature
	 */
	public String getTemperature() {
		return temperature;
	}
	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	/**
	 * @return the humidity
	 */
	public String getHumidity() {
		return humidity;
	}
	/**
	 * @param humidity the humidity to set
	 */
	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
}
