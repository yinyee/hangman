package com.github.yinyee.hangman;

import java.util.HashMap;
import java.util.Map;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

public class Game {

	private String cookie;
	private String wordToGuess;
	private String displayedWord;
	private String previousGuesses;
	private int remainingBlanks;
	private int currentIndex;
	
	protected Game(String cookie, String wordToGuess) {
		
		this.cookie = cookie;
		this.wordToGuess = wordToGuess;
		
		char[] cDisplayedWord = new char[wordToGuess.length()];
		for (int i = 0; i < wordToGuess.length(); i++) cDisplayedWord[i] = '?';
		this.displayedWord = String.copyValueOf(cDisplayedWord);

		this.previousGuesses = null;
		this.remainingBlanks = wordToGuess.length();
		this.currentIndex = 0;
	}
	
	private Game(String cookie, String wordToGuess, String displayedWord, String previousGuesses, int remainingBlanks, int currentIndex) {
		
		this.cookie = cookie;
		this.wordToGuess = wordToGuess;
		this.displayedWord = displayedWord;
		this.previousGuesses = previousGuesses;
		this.remainingBlanks = remainingBlanks;
		this.currentIndex = currentIndex;	
	}
	
	/**
	 * Create a new game in the database.
	 */
	protected static void createNewGame(Game game) {
		
		Map<String, AttributeValue> key = new HashMap<String,AttributeValue>();
		
		key.put("cookie", new AttributeValue(game.cookie));
		key.put("word_to_guess", new AttributeValue(game.wordToGuess));
		key.put("displayed_word", new AttributeValue(game.displayedWord));
		key.put("remaining_blanks", new AttributeValue(String.valueOf(game.wordToGuess.length())));
		key.put("current_index", new AttributeValue(String.valueOf(game.currentIndex)));
		
		Database.getDbClient().putItem(new PutItemRequest().withTableName("hangman").withItem(key));
		
	}
	
	protected static Game retrieveGame(String cookie) {
		
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("cookie", new AttributeValue(cookie));
		
		Map<String, AttributeValue> value = Database.getDbClient().getItem(new GetItemRequest().withTableName("hangman").withKey(key)).getItem();
		
		Game game;
		
		// If game has been deleted from database
		if (value == null) game = null;
		else {
			
			String wordToGuess = value.get("word_to_guess").getS();
			String displayedWord = value.get("displayed_word").getS();

			String previousGuesses = null;
			if (value.get("previous_guesses") != null) previousGuesses = value.get("previous_guesses").getS();

			int remainingBlanks = Integer.valueOf(value.get("remaining_blanks").getS());
			int currentIndex = Integer.valueOf(value.get("current_index").getS());
			
			game = new Game(cookie, wordToGuess, displayedWord, previousGuesses, remainingBlanks, currentIndex); 
		}

		return game;
	}
	
	protected static void deleteGame(String cookie) {
		
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("cookie", new AttributeValue(cookie));
		
		Database.getDbClient().deleteItem("hangman", key);
		
	}
	
	protected static void updateGame(Game game) {
		
		UpdateItemSpec updateItemSpec = new UpdateItemSpec()
				.withPrimaryKey("cookie", game.getUserCookie())
				.withUpdateExpression("set displayed_word = :d, previous_guesses = :g, remaining_blanks = :b, current_index = :w")
				.withValueMap(new ValueMap()
						.withString(":d", game.getDisplayedWord())
						.withString(":g", game.getPreviousGuesses())
						.withString(":b", String.valueOf(game.getRemainingBlanks()))
						.withString(":w", String.valueOf(game.getCurrentIndex())));
		
		DynamoDB dynamoDB = new DynamoDB(Database.getDbClient());
		Table table = dynamoDB.getTable("hangman");
		
		try {
			table.updateItem(updateItemSpec);
		} catch (Exception e) {
			System.err.println("Failed to update DynamoDB");
			e.printStackTrace();
		}
		
	}
	
	protected String getUserCookie() {
		return this.cookie;
	}
	
	protected String getWordToGuess() {
		return this.wordToGuess;
	}
	
	protected String getDisplayedWord() {
		return this.displayedWord;
	}
	
	protected void setDisplayedWord(String updatedDisplayedWord) {
		this.displayedWord = updatedDisplayedWord;
	}
	
	protected String getPreviousGuesses() {
		return this.previousGuesses;
	}
	
	protected void setPreviousGuesses(String updatedPreviousGuesses) {
		this.previousGuesses = updatedPreviousGuesses;
	}
	
	protected int getRemainingBlanks() {
		return this.remainingBlanks;
	}
	
	protected void setRemainingBlanks(int updatedRemainingBlanks) {
		this.remainingBlanks = updatedRemainingBlanks;
	}
	
	protected int getCurrentIndex() {
		return this.currentIndex;
	}
	
	protected void setCurrentIndex(int updatedCurrentIndex) {
		this.currentIndex = updatedCurrentIndex;
	}
}
