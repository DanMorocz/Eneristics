/**
 * 
 */
package eneristics.mongo.api;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * @author Dan
 *
 */
public class TestHeatingCoolingMode {
	
	public static final String USER = "testremote";
	public static final String PASS = "test";
	//public static final String HOST = "192.168.0.17";
	public static final String HOST = "localhost";
	public static final int PORT = 27017;
	public static final String DEVICE_DB_NAME = "readings_NEW";
	public static final String SENSOR_COLLECTION = "sensortags";
	public static final String WEATHER_COLLECTION = "weather";
	
	MongoClient mongo = null;
	DB db = null;

	public String getCurrentMode(long timestamp, int numPolls) {
		
		connectMongo();
		
		//public Cursor aggregate(List<? extends DBObject> pipeline, AggregationOptions options)
		//DBCursor lastThreePolls = new DBCursor(db.getCollection(SENSOR_COLLECTION), DBObject query, DBObject fields, ReadPreference readPreference);
			
		BasicDBObject queryParams = new BasicDBObject();
		queryParams.append("timestamp", -1);
		DBCursor lastThreePolls = db.getCollection(SENSOR_COLLECTION).find().sort( queryParams );
		
		int counter = 0;
		float totalTemp = 0;
		String output = "";
		while(lastThreePolls.hasNext() && counter++<numPolls) {
			DBObject obj = lastThreePolls.next();
			output+= "\n"+obj.toString();
			output+= "\n"+"Temp: " + (obj.get("temperature")).toString();
			totalTemp += Float.parseFloat(obj.get("temperature").toString());
			output += "\n" + obj.get("temperature").toString() + " ";
		}
		
		output+= "\n"+"Avarage Temp over the last " + numPolls + " polls is: " + (totalTemp/numPolls);
		//JOptionPane.showMessageDialog(null, output);
		//return 0;
		return output;
	}
	
	
	private void connectMongo() {
		mongo = getMongoClient();
		db = getDBFromMongo(mongo);
		
		//System.out.println(db.collectionExists(SENSOR_COLLECTION));
	}
	
	private DB getDBFromMongo(MongoClient mongo) {
		return new DB(mongo, DEVICE_DB_NAME);
	}
	
	private MongoClient getMongoClient() {
		
		ServerAddress address = new ServerAddress(HOST, PORT);
		List<ServerAddress> addList = new ArrayList<ServerAddress>();
		addList.add(address);
		
		MongoCredential	cred = MongoCredential.createCredential(USER, DEVICE_DB_NAME, PASS.toCharArray());
		List<MongoCredential> credList = new ArrayList<MongoCredential>();
		credList.add(cred);
		
		return new MongoClient(addList);//, credList);
	}
}
