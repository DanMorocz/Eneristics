package com.eneristics.test;

import javax.swing.JOptionPane;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class StaticMongo {

	private static MongoClient mongoInstance = null;
	
	public static MongoClient getInstance() {
		
		if(null == mongoInstance ) {
			try {
				MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
				mongoInstance = new MongoClient(uri);
				//default title and icon
				JOptionPane.showMessageDialog(null, "Mongo Instance built!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return mongoInstance;
	}
}
