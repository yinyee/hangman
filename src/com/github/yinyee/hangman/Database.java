package com.github.yinyee.hangman;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class Database {
	
	private static AmazonDynamoDBClient sDbClient;
	
	public synchronized static AmazonDynamoDBClient getDbClient() {
		if (sDbClient == null) {
			AmazonDynamoDBClient dbClient = new AmazonDynamoDBClient();
			dbClient.withRegion(Regions.EU_WEST_1);
			sDbClient = dbClient;
		}
		return sDbClient;
	}

}
