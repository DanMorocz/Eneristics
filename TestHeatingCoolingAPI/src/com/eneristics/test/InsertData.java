package com.eneristics.test;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.input.SAXBuilder;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.diagnostics.logging.Logger;

public class InsertData {
	
	public static final String USER = "testremote";
	public static final String PASS = "test";
	//public static final String HOST = "192.168.0.17";
	public static final String HOST = "localhost";
	public static final int PORT = 27017;
	public static final String DEVICE_DB_NAME = "readings";
	public static final String SENSOR_COLLECTION = "sensortags";
	public static final String WEATHER_COLLECTION = "weather";

	private static final String MONGO_DEVICE_ID = "device_id"; 
	private static final String DEFAULT_DEVICE_ID = "00:00:00:00:00:00";
	private static final String MONGO_TIMESTAMP = "timestamp"; //: 1483486991077
	private static final String MONGO_TEMPERATURE = "temperature"; //: 21.0
	private static final String MONGO_HUMIDITY = "humidity"; //: 50
	
	private static MongoClient mongo = null;
	private static DB db = null;
	

	public static void main(String[] args) {
		connectMongo();
		DBCollection collection= db.getCollection(SENSOR_COLLECTION);
		//printMongo(collection);
		insertData(collection, args);
		//close the MongoClient instance
		//mongo.close();
	}
	
	private static BasicDBObject buildDocument(String device, String timestamp, String temp, String humidity) {

		BasicDBObject document = new BasicDBObject();
		document.put(MONGO_DEVICE_ID, device);
		document.put(MONGO_TIMESTAMP, timestamp);
		document.put(MONGO_TEMPERATURE, temp);
		document.put(MONGO_HUMIDITY, humidity);
		
		return document;
	}
	
	
	private static void connectMongo() {
		mongo = StaticMongo.getInstance(); //getMongoClient();
		db = getDBFromMongo(mongo);
	}
	

	/*private static MongoClient getMongoClient() {
		
		ServerAddress address = new ServerAddress(HOST, PORT);
		List<ServerAddress> addList = new ArrayList<ServerAddress>();
		addList.add(address);
		
		MongoCredential	cred = MongoCredential.createCredential(USER, DEVICE_DB_NAME, PASS.toCharArray());
		List<MongoCredential> credList = new ArrayList<MongoCredential>();
		credList.add(cred);
		
		return new MongoClient(addList);//, credList);
	}*/
	
	private static DB getDBFromMongo(MongoClient mongo) {
		return new DB(mongo, DEVICE_DB_NAME);
	}
	
	private static void printMongo(DBCollection collection) {
		DBCursor allCursor = collection.find();
		
		while(allCursor.hasNext()) {
			System.out.println(allCursor.next().toString());
		}
	}

	//00:00:00:00:00:00 1482382809 20.59 55.93
	//collection.insert(buildDocument(DEFAULT_DEVICE_ID, "1482382809", "20.59","55.93"));
	private static void insertData(DBCollection collection, String [] args) {
		
		try {
			collection.insert(buildDocument(args[0], args[1], args[2], args[3]));
		}
		catch(Exception e) {
			System.out.println("Error:\n" + e.toString());
			//logger.info("Error inserting data to MongoDB.");
		}
	}
}
