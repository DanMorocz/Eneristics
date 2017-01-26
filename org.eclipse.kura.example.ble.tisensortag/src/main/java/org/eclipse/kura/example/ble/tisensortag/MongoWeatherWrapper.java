package org.eclipse.kura.example.ble.tisensortag;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;



public class MongoWeatherWrapper {
	
	private static final Logger s_logger = LoggerFactory.getLogger(MongoWeatherWrapper.class);
	
	MongoClient mongo = null;
	DB db = null;
	
	public MongoWeatherWrapper() {
		if(null == mongo) {
			connectMongo();
		}
		if(null == db) {
			db = getDBFromMongo(mongo);
		}
	}
	
	private void connectMongo() {
		mongo = getMongoClient();
		db = getDBFromMongo(mongo);
	}
	
	private MongoClient getMongoClient() {
		
//		ServerAddress address = new ServerAddress(ConnectionInfo.HOST, ConnectionInfo.PORT);
//		List<ServerAddress> addList = new ArrayList<ServerAddress>();
//		addList.add(address);
//		
//		MongoCredential	cred = MongoCredential.createCredential(ConnectionInfo.USER, ConnectionInfo.DEVICE_DB_NAME, ConnectionInfo.PASS.toCharArray());
//		List<MongoCredential> credList = new ArrayList<MongoCredential>();
//		credList.add(cred);
//		
//		return new MongoClient(addList);//, credList);
		MongoClient client = null;
		
		try {
			MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
			client = new MongoClient(uri);
		} catch (Exception e) {
			s_logger.info("Exception getting DB From Mongo" + e.toString());
		}
		return client;
		
		//return new MongoClient(new MongoClientURI("mongodb://localhost:27017/readings"));
	}
	
	private DB getDBFromMongo(MongoClient mongo) {
		return new DB(mongo, ConnectionInfo.DEVICE_DB_NAME);
	}
	
	public void publishMongo() {
		s_logger.info("IN PUBLISH MONGO ABOUT TO BUILD WEATHER DOCUMET ");
		try {
			s_logger.info("ABOUT TO BUILD WEATHER DOCUMET ");
			publishMongo(mongo, db, new WeatherDocument());
		}
		catch(UnknownHostException uhe) {
			System.out.println("Unable to publish polled weather data to MongoDB");
		}
		
	}
	
	// Built for publishing to MongoDB
	private void publishMongo(MongoClient mongo, DB database, WeatherDocument weather) throws UnknownHostException {
		s_logger.info("IN PUBLISH MONGO MAIN ABOUT TO BUILD WEATHER DOCUMET ");
		DBObject doc = createDBObject(weather);
		
		DBCollection collection = database.getCollection(ConnectionInfo.WEATHER_COLLECTION);
		
		// save the document to the collecion called ConnectionInfo.COLLECTION_NAME
		collection.save(doc);
 
		// print the entire contents of the database
		DBCursor cursor = collection.find();
		while(cursor.hasNext()) {
			System.out.println(cursor.next().toString());
		}
		
		System.out.println("There are currently " + collection.count() + " records in the " + ConnectionInfo.WEATHER_COLLECTION + " collection.");

		//close resources
		//mongo.close();
	}

	private DBObject createDBObject(WeatherDocument reading) {
		BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();
		
		docBuilder.append(WeatherDocument.WEATHER_TIME_MONGO_NAME, reading.getTimestamp());
		docBuilder.append(WeatherDocument.TEMPERATURE_MONGO_NAME, reading.getTemperature());
		docBuilder.append(WeatherDocument.WINDCHILL_MONGO_NAME, reading.getWindChill());
		docBuilder.append(WeatherDocument.PRESSURE_MONGO_NAME, reading.getPressure());
		docBuilder.append(WeatherDocument.HUMIDITY_MONGO_NAME, reading.getHumidity());
		docBuilder.append(WeatherDocument.WINDSPEED_MONGO_NAME, reading.getWindSpeed());
		docBuilder.append(WeatherDocument.WINDSPEED_UNITS_MONGO_NAME, reading.getWindUnits());
		docBuilder.append(WeatherDocument.WIND_DIRECTION_MONGO_NAME, reading.getWindDirection());
				
		return docBuilder.get();
	}
}
