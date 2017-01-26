package org.eclipse.kura.example.ble.tisensortag;

import java.nio.charset.StandardCharsets;

import org.jdom2.input.SAXBuilder;
import org.jdom2.Document;
import org.jdom2.Element;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;




public class WeatherDocument {
	private static Logger s_logger = null;
	
	public static final String WEATHER_TIME_MONGO_NAME = "timestamp";
	public static final String TEMPERATURE_MONGO_NAME = "temperature";
	public static final String WINDCHILL_MONGO_NAME = "windchill";
	public static final String PRESSURE_MONGO_NAME = "pressure";
	public static final String HUMIDITY_MONGO_NAME = "humidity";
	public static final String WINDSPEED_MONGO_NAME = "windspeed";
	public static final String WINDSPEED_UNITS_MONGO_NAME = "wind_units";
	public static final String WIND_DIRECTION_MONGO_NAME = "wind_direction";
	
	public static String WEATHER_URL = "http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/ON/s0000430_e.xml";
	
	public static String CURRENT_CONDITIONS_KEY = "currentConditions";
	public static String TEMP_UNITS_KEY = "units";
	public static String NOT_USED = "not used";
	
	public static String TEMPERATURE_KEY = "temperature";
	public static String WINDCHILL_KEY = "windChill";
	public static String PRESSURE_KEY = "pressure";
	public static String HUMIDITY_KEY = "relativeHumidity";
	public static String WIND_CHILL_KEY = "wind";
	public static String WIND_SPEED_KEY = "speed";
	public static String WIND_DIRECTION_KEY = "direction";
	
	public static float DEFAULT_TEMPERATURE = 999;
	public static int DEFAULT_WINDCHILL = 0;
	public static float DEFAULT_PRESSURE = 999;
	public static int DEFAULT_HUMIDITY = 999;
	public static int DEFAULT_WINDSPEED = 999;
	public static String DEFAULT_WINDSPEED_UNITS = "N/A";
	public static String DEFAULT_WIND_DIRECTION = "N/A";

	private SAXBuilder saxBuilder = null;
	private Long timestamp;
	private float temperature; //default value, represents missing value from weather XML
	private int windChill;
	private float pressure;
	private int humidity;
	private int windSpeed;
	private String windUnits;
	private String windDirection;	
	
	/**
	 * @author: Dan Morocz
	 * Build a WeatherDocument from a current poll of weatheroffice
	 */
	public WeatherDocument() {
		
		timestamp = System.currentTimeMillis();
		temperature = getTempFromServer();
		windChill = getWindChillFromServer();
		pressure = getPressureFromServer();
		humidity = getHumidityFromServer();
		windSpeed = getWindSpeedFromServer();
		windUnits = getWindSpeedUnitsFromServer();
		windDirection = getWindDirection();	
	}
	
	private void buildSAX() {
		if( null == saxBuilder ) 
			saxBuilder = new SAXBuilder();
	}
	
	public byte[] getBytes() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("Timestamp", System.currentTimeMillis());
			obj.put(TEMPERATURE_KEY, getTemperature());
			obj.put(WINDCHILL_KEY, getWindChill());
			obj.put(PRESSURE_KEY, getPressure());
			obj.put(HUMIDITY_KEY, getHumidity());
			obj.put(WIND_CHILL_KEY, getWindChill());
			obj.put(WIND_SPEED_KEY, getWindSpeed());
			obj.put(WIND_DIRECTION_KEY, getWindDirection());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return obj.toString().getBytes(StandardCharsets.UTF_8);
	}
	
	//get the temperature
	private float getTempFromServer() {
		try {
			s_logger.info("TRYING SAX BUILDER ");
			buildSAX();
			s_logger.info("PAST SAX BUILDER ");
			s_logger.info("TRYING WEATHER URL ");
			Document document = (Document) saxBuilder.build(WEATHER_URL);
			s_logger.info("PAST WEATHER URL ");
			Element rootNode = document.getRootElement();
			
			Element currentConditions = rootNode.getChild(CURRENT_CONDITIONS_KEY);
			
			//get the temp Element from current conditions
			Element tempElement = currentConditions.getChild(TEMPERATURE_KEY);
			
			if(null == tempElement) 
				return DEFAULT_TEMPERATURE;
			
			//get the actual temp from the temp element
			try {
				return Float.parseFloat(tempElement.getValue());
				//System.out.println("ACTUAL TEMPERATURE: " + temperature + tempElement.getAttributeValue(TEMP_UNITS_KEY));
			} 
			catch(NumberFormatException nfe) {
				System.out.println("Temperature is not a floating point number.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_TEMPERATURE;
	}
	
	//get the windchill
	private int getWindChillFromServer() {
		try {
			buildSAX();

			Document document = (Document) saxBuilder.build(WEATHER_URL);
			Element rootNode = document.getRootElement();
			
			Element currentConditions = rootNode.getChild(CURRENT_CONDITIONS_KEY);
			
			//get the windchill Element from current conditions
			Element tempElement = currentConditions.getChild(WINDCHILL_KEY);
			
			if(null == tempElement) 
				return DEFAULT_WINDCHILL;
			
			//if there is a windchill factor
			if(null != tempElement) {
				//get the actual windchill from the windchill element
				try {
					return Integer.parseInt(tempElement.getValue());
					//System.out.println("ACTUAL TEMPERATURE: " + temperature + tempElement.getAttributeValue(TEMP_UNITS_KEY));
				} 
				catch(NumberFormatException nfe) {
					System.out.println("Windchill is not an integer value.");
				}
			}
			else {
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_WINDCHILL;
	}
	
	//get the pressure
	private float getPressureFromServer() {
		try {
			buildSAX();
			
			Document document = (Document) saxBuilder.build(WEATHER_URL);
			Element rootNode = document.getRootElement();
			
			Element currentConditions = rootNode.getChild(CURRENT_CONDITIONS_KEY);
			
			//get the pressure Element from current conditions
			Element tempElement = currentConditions.getChild(PRESSURE_KEY);
			if(null == tempElement) 
				return DEFAULT_PRESSURE;
			
			//get the actual pressure from the pressure element
			try {
				return Float.parseFloat(tempElement.getValue());
				//System.out.println("ACTUAL TEMPERATURE: " + temperature + tempElement.getAttributeValue(TEMP_UNITS_KEY));
			} 
			catch(NumberFormatException nfe) {
				System.out.println("Pressure is not a floating point number.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_PRESSURE;
	}
	
	//get the relative humidity
	private int getHumidityFromServer() {
		try {
			buildSAX();
			
			Document document = (Document) saxBuilder.build(WEATHER_URL);
			Element rootNode = document.getRootElement();
			
			Element currentConditions = rootNode.getChild(CURRENT_CONDITIONS_KEY);
			
			//get the humidity Element from current conditions
			Element tempElement = currentConditions.getChild(HUMIDITY_KEY);
			
			if(null == tempElement) 
				return DEFAULT_HUMIDITY;
			
			//get the actual humidity from the humidity element
			try {
				return Integer.parseInt(tempElement.getValue());
				//System.out.println("ACTUAL TEMPERATURE: " + temperature + tempElement.getAttributeValue(TEMP_UNITS_KEY));
			} 
			catch(NumberFormatException nfe) {
				System.out.println("Humidity is not an integer value.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_HUMIDITY;
	}
	
	//get the windspeed
	private int getWindSpeedFromServer() {
		try {
			buildSAX();
			
			Document document = (Document) saxBuilder.build(WEATHER_URL);
			Element rootNode = document.getRootElement();
			
			Element currentConditions = rootNode.getChild(CURRENT_CONDITIONS_KEY);
			
			//get the humidity Element from current conditions
			Element wind = currentConditions.getChild(WIND_CHILL_KEY);
			Element windSpeed = wind.getChild(WIND_SPEED_KEY);
			String windSpdUnits = windSpeed.getAttributeValue("units");
			
			if(null == windSpeed) 
				return DEFAULT_WINDSPEED;
			
			//get the actual humidity from the humidity element
			try {
				return Integer.parseInt(windSpeed.getValue());
				//System.out.println("ACTUAL TEMPERATURE: " + temperature + tempElement.getAttributeValue(TEMP_UNITS_KEY));
			} 
			catch(NumberFormatException nfe) {
				System.out.println("Wind speed is not an integer value.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_WINDSPEED;
	}
	
	//get the windspeed units
	private String getWindSpeedUnitsFromServer() {
		try {
			buildSAX();
			
			Document document = (Document) saxBuilder.build(WEATHER_URL);
			Element rootNode = document.getRootElement();
			
			Element currentConditions = rootNode.getChild(CURRENT_CONDITIONS_KEY);
			
			//get the humidity Element from current conditions
			Element wind = currentConditions.getChild(WIND_CHILL_KEY);
			Element windSpeed = wind.getChild(WIND_SPEED_KEY);
			String windSpdUnits = windSpeed.getAttributeValue("units");
			
			if(null == windSpdUnits) 
				return DEFAULT_WINDSPEED_UNITS;
			
			//get the actual humidity from the humidity element
			try {
				return windSpdUnits;
				//System.out.println("ACTUAL TEMPERATURE: " + temperature + tempElement.getAttributeValue(TEMP_UNITS_KEY));
			} 
			catch(NumberFormatException nfe) {
				System.out.println("Wind speed is not an integer value.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_WINDSPEED_UNITS;
	}
	
	//get the wind direction
	private String getWindDirectionFromServer() {
		try {
			buildSAX();
			
			Document document = (Document) saxBuilder.build(WEATHER_URL);
			Element rootNode = document.getRootElement();
			
			Element currentConditions = rootNode.getChild(CURRENT_CONDITIONS_KEY);
			
			//get the humidity Element from current conditions
			Element wind = currentConditions.getChild(WIND_CHILL_KEY);
			Element windDirection = wind.getChild(WIND_DIRECTION_KEY);
			
			if(null == windDirection) 
				return DEFAULT_WIND_DIRECTION;
			
			//get the actual humidity from the humidity element
			try {
				return windDirection.getValue();
				//System.out.println("ACTUAL TEMPERATURE: " + temperature + tempElement.getAttributeValue(TEMP_UNITS_KEY));
			} 
			catch(NumberFormatException nfe) {
				System.out.println("Wind direction is not available.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_WIND_DIRECTION;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public float getTemperature() {
		return temperature;
	}
	
	public int getWindChill() {
		return windChill;
	}
	
	public float getPressure() {
		return pressure;
	}
	
	public int getHumidity() {
		return humidity;
	}
	
	public int getWindSpeed() {
		return windSpeed;
	}
	
	public String getWindUnits() {
		return windUnits;
	}
	
	public String getWindDirection() {
		return windDirection;
	}
	
}
