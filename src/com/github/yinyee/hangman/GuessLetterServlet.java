package com.github.yinyee.hangman;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GuessLetterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Retrieve cookie
		String cookie = null;
		
		if (request.getCookies() != null) {
			Cookie[] cookies = request.getCookies();
			for (Cookie c: cookies) {
				if (c.getName().compareTo("hangman") == 0) cookie = c.getValue(); 
			}
		}
		
		// Retrieve game from database
		Game game = Game.retrieveGame(cookie);
		
		String wordToGuess = game.getWordToGuess();
		char[] cWordToGuess = wordToGuess.toCharArray();
		
		String displayedWord = game.getDisplayedWord();
		char[] cDisplayedWord = displayedWord.toCharArray();
		
		String previousGuesses = game.getPreviousGuesses();
		
		int remainingBlanks = game.getRemainingBlanks();
		int currentIndex = game.getCurrentIndex();
		
		// Retrieve guessed letter from request
		String req = request.getReader().readLine();
		req = req.trim();
		char guess = req.charAt(0);
		guess = Character.toUpperCase(guess);
		
		// Check if guess is correct
		boolean correct = false;
		for (int i = 0; i < cWordToGuess.length; i++) {
			if (cWordToGuess[i] == guess) {
				cDisplayedWord[i] = guess;
				remainingBlanks--;
				correct = true;
			}
		}
		if (!correct) currentIndex++;
		
		if (previousGuesses == null) previousGuesses = String.valueOf(guess);
		else previousGuesses += guess;
		
		displayedWord = String.copyValueOf(cDisplayedWord);
		
		// If game is over
		if (currentIndex == 9 || remainingBlanks == 0) {
			
			response.addHeader("game-over", String.valueOf(1));
			
			// Delete game from database
			Game.deleteGame(cookie);
			
		} else /* Keep playing */{
			
			response.addHeader("game-over", String.valueOf(0));
			
			// Update game in database
			game.setDisplayedWord(displayedWord);
			game.setPreviousGuesses(previousGuesses);
			game.setRemainingBlanks(remainingBlanks);
			game.setCurrentIndex(currentIndex);
			Game.updateGame(game);
		}
		
		// Update HTTP response headers
		response.addHeader("displayed-word", displayedWord);
		response.addHeader("current-index", String.valueOf(currentIndex));
		if (previousGuesses != null) response.addHeader("previous-guesses", previousGuesses);	
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}