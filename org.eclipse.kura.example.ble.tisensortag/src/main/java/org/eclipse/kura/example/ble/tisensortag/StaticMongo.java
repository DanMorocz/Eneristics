package org.eclipse.kura.example.ble.tisensortag;

import org.slf4j.Logger;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class StaticMongo {

	private static MongoClient mongoInstance = null;
	
	public static MongoClient getInstance(Logger logger) {
		
		if(null == mongoInstance ) {
			try {
				MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
				mongoInstance = new MongoClient(uri);
			} catch (Exception e) {
				logger.info("Exception getting DB From Mongo" + e.toString());
			}
		}
		
		return mongoInstance;
	}
}
