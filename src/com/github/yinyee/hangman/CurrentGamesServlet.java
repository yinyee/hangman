package com.github.yinyee.hangman;

import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class CurrentGamesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String cookie = null;
		String state = null;
		int currentIndex = -1;
		int guessesRemaining = -1;
		
		String open = "<tr><td>";
		String td = "</td><td>";
		String close = "</td></tr>";
		
	    ScanResult result = null;
	    
	    // Write out table
	    PrintWriter out = response.getWriter();
	 
	    do {
	        ScanRequest req = new ScanRequest();
	        req.setTableName("hangman");
	 
	        if (result != null) req.setExclusiveStartKey(result.getLastEvaluatedKey());  
	        result = Database.getDbClient().scan(req);
	        List<Map<String, AttributeValue>> rows = result.getItems();
	 
	        for (Map<String, AttributeValue> row : rows) {
	        	
	            try {
	            	
	            	cookie = row.get("cookie").getS();
	                state = row.get("displayed_word").getS();
	                currentIndex = Integer.valueOf(row.get("current_index").getS());
	                guessesRemaining = 9 - currentIndex;
	                
	                out.println(open + cookie + td + state + td + String.valueOf(guessesRemaining) + close);
	                
	            } catch (NumberFormatException e){
	                e.printStackTrace();
	            }
	        }
	    } while (result.getLastEvaluatedKey() != null);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}