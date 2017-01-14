package com.github.yinyee.hangman;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * If there is a current game associated with this cookie, then return the current game.
 * Otherwise, create a new game.
 */

public class NewGameServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String cookie = null;
		Game game = null;
		
		// Check for cookie
		if (request.getCookies() != null) {
			Cookie[] cookies = request.getCookies();
			for (Cookie c: cookies) {
				if (c.getName().compareTo("hangman") == 0) cookie = c.getValue(); 
			}
		}
		
		// If cookie exists
		if (cookie != null) {
			
			// Retrieve game from database
			game = Game.retrieveGame(cookie);
			
			// If game deleted from database, create a new game
			if (game == null) {
				game = new Game(cookie, newWord());
				Game.createNewGame(game);
			}
			
		} else /* Set cookie and create a new game */ {
			
			cookie = String.valueOf((Math.random() * (10^20)));
			game = new Game(cookie, newWord());
			Game.createNewGame(game);
			
			Cookie hangmanCookie = new Cookie("hangman", cookie);
			response.addCookie(hangmanCookie);
		}
		
		// Update HTTP response headers
		response.addHeader("displayed-word", game.getDisplayedWord());
		response.addHeader("current-index", String.valueOf(game.getCurrentIndex()));
		if (game.getPreviousGuesses() != null) response.addHeader("previous-guesses", game.getPreviousGuesses());
		response.addHeader("game-over", String.valueOf(0));

	}

	/**
	 * Retrieves word from bag of words and convert to all caps
	 * @return new wordToGuess
	 * @throws IOException
	 */
	private String newWord() throws IOException {
		
		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/WEB-INF/properties/words.properties"));
		
		int randomIndex = (int) ((Math.random() * 228982) + 1);
		String wordToGuess = properties.getProperty(String.valueOf(randomIndex));
		wordToGuess = wordToGuess.toUpperCase();
		
		return wordToGuess;
	}

}