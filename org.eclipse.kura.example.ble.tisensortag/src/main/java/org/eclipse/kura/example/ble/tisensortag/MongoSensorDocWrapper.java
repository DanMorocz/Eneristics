package org.eclipse.kura.example.ble.tisensortag;

import java.net.UnknownHostException;
import java.util.Date;

import org.eclipse.kura.message.KuraPayload;
import org.slf4j.Logger;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


public class MongoSensorDocWrapper {
	//private static final Logger s_logger = LoggerFactory.getLogger(BluetoothLe.class);
	private static Logger s_logger = null;
	int idCode;
	private static MongoClient mongo = null;
	private DB db = null;
	
	public static final String DB_name = "readings";
	
	/*public MongoSensorDocWrapper() {
		s_logger.info("Latest Testing In MongoSensorWroaperDoc constructor ");
	}*/
	
	public MongoSensorDocWrapper(Logger newLogger) {
		s_logger=newLogger;
		s_logger.info("Latest Testing In MongoSensorWroaperDoc constructor ");
		if(null == mongo) {
			s_logger.info("Latest Testing calling connect MOngo ");
			connectMongo();
		}
		if(null == db) {
			db = getDBFromMongo(mongo);
		}
	}

	private void connectMongo() {
		if(null == mongo) {
			mongo =  StaticMongo.getInstance(s_logger);
		}
		
		db = getDBFromMongo(mongo);
	}
	
	//For testing, build a sample KuraPayload and publish it to the MongoDB specified in ConnectionInfo
	public void publish() {
		connectMongo();
		try {
			publishMongo(mongo, db, buildSensorKuraPayload());
		}
		catch(UnknownHostException uhe) {
			System.out.println("Unknown Host");
		}
	}
	/* This is testcode From Test with Dave Woodard 
	/*public void publish() {
    s_logger.info("Building MongoClient URI ");
	MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
	
	s_logger.info("Opening MongoClient ");

	MongoClient mongoClient = new MongoClient(connectionString);
	s_logger.info("PAST OPENING MongoClient ");
	
	MongoDatabase db = mongoClient.getDatabase("test");
	s_logger.info("**** SET DATABASE TO TEST ");
	
	// Create a collection and insert document
	MongoCollection<Document> collection = db.getCollection("test");
	s_logger.info("***** SET COLLECTION TO TEST ");
	Document document = new Document("name", "MongoTest")
		.append("type", "database")
		.append("count", 1);
	collection.insertOne(document);
	s_logger.info("***** INSERT THE DOCUMENT ");
	
	// Retrieve document to verify
	Document findDoc = collection.find().first();
	s_logger.info(findDoc.toJson());
	s_logger.info("RETRIEVING DOCUMNET TO TEST ");
	
	// Flush DB so this example can run next time
	//db.drop();
	
	// Close connection
	mongoClient.close();
	s_logger.info("*** CLOSING MONGO CONNECTION");
	}*/
	
	
	public void publish(KuraPayload payload) {
		connectMongo();
		try {
			publishMongo(mongo, db, payload);
		}
		catch(UnknownHostException uhe) {
			System.out.println("Unknown Host");
		}
	}	
	
	private DB getDBFromMongo(MongoClient mongo) {
		return new DB(mongo, ConnectionInfo.DEVICE_DB_NAME);
	}
	
	// Built for publishing to MongoDB
	public void publishMongo(MongoClient mongo, DB database, KuraPayload payload) throws UnknownHostException {
		s_logger.info("******* Creating SensorDocPlayLoad ");
		SensorDocument reading = createSensorDoc(payload);
		s_logger.info(" *******SensorDoc PayLoad Created ");
		DBObject doc = createDBObject(reading);
		
		DBCollection collection = database.getCollection(ConnectionInfo.SENSOR_COLLECTION);
		
		// save the document to the collecion called ConnectionInfo.COLLECTION_NAME
		collection.save(doc);
 
		// print the entire contents of the database; test only 
		//DBCursor cursor = collection.find();
		//while(cursor.hasNext()) {
		//	System.out.println(cursor.next().toString());
		//}
		
		System.out.println("There are currently " + collection.count() + " records in the " + ConnectionInfo.SENSOR_COLLECTION + " collection.");

		//close resources
		//mongo.close();
	}

	private DBObject createDBObject(SensorDocument reading) {
		BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();
								
		//docBuilder.append("_id", user.getId()); let Mongo generate a unique key for this document
		docBuilder.append(SensorDocument.DEVICE_ID_MONGO_NAME, reading.getDevice());
		docBuilder.append(SensorDocument.DEVICE_TIME_MONGO_NAME, reading.getTimestamp());
		docBuilder.append(SensorDocument.DEVICE_TEMP_MONGO_NAME, reading.getTemperature());
		docBuilder.append(SensorDocument.DEVICE_HUMIDITY_MONGO_NAME, reading.getHumidity());
		return docBuilder.get();
	}

	//Build a SensorDocument from a KuraPayload
	private SensorDocument createSensorDoc(KuraPayload payload) {
		
		SensorDocument s = new SensorDocument();
		s.setDevice((String)payload.getMetric(SensorDocument.DEVICE_ID_KURA_NAME));//get device_Id payload  and set it in s doc
		s.setTimestamp((String)payload.getMetric(SensorDocument.DEVICE_TIME_KURA_NAME));//change to string Jan 23
		try {
			s.setTemperature((String)payload.getMetric(SensorDocument.DEVICE_TEMP_KURA_NAME));
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		s.setHumidity((String)payload.getMetric(SensorDocument.DEVICE_HUMIDITY_KURA_NAME));
		return s;
	}

	//Build a default, testing KuraPayload
	private KuraPayload buildSensorKuraPayload() {
		//generate a default payload, for testing
		KuraPayload payload = new KuraPayload();
		payload.setTimestamp(new Date());
		
		payload.addMetric(SensorDocument.DEVICE_ID_MONGO_NAME, "MONGO " + idCode);
		payload.addMetric(SensorDocument.DEVICE_TIME_MONGO_NAME, ""+System.currentTimeMillis());//jan 23 change to string
		payload.addMetric(SensorDocument.DEVICE_TEMP_MONGO_NAME, "21.0");
		payload.addMetric(SensorDocument.DEVICE_HUMIDITY_MONGO_NAME, "50");
		
		return payload;
	}
}




