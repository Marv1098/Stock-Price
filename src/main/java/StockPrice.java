/*
 * Author: Marvin Tailor
 * Company: Tata Consultancy Service
 * Project Start Date: 01/25/2021
 * Last Modified Date: 01/29/2021
 * Description: This application takes user input of stock symbol such as (TSLA, GM, GME, GOOGL & so on) and it retrieves the Market Price, Change, Stock Name, Stock Symbol, EPS & Dividends. 
 * 				Then displays it to the console and sends the data that is retrieved from YahooFinance to the Firebase Cloud Firestore. 
 * 				The application asks for user input until the user types "q" to quit, so you can look up as many stocks as you want and when you are finished just enter "q" to quit.
 */

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockPrice {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		
		// Use the application default credentials
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
		String projectId = "stockprice-200d6";
		FirebaseOptions builder = FirebaseOptions.builder()
				.setCredentials(credentials)
				.setProjectId(projectId)
				.build();
						
		FirebaseApp.initializeApp(builder);
		System.out.println("Examples of some stock symbols [ INTC, AMZN, TSLA, GOOG ]");
		Scanner symbol = new Scanner(System.in);
		while(true) {
			System.out.println(" ");
			System.out.print("Enter Stock Symbol (q to quit): ");
			String stockName = symbol.nextLine();
			if(!stockName.equals("q")) {
				Object[] val = getStock(stockName);
				sendStock(val);
			} else {
				System.out.println(" ");
				System.out.println("Program Terminated!!");
				break;
			}
		}
		symbol.close();

	}
	
	// Function to get the Stock price with name, dividends and EPS (Earnings Generated Per Share)
	public static Object[] getStock(String symbol) throws IOException, InterruptedException, ExecutionException {
		Stock stock = YahooFinance.get(symbol, true);
		
		String sym = stock.getSymbol();
		String name = stock.getName();
		BigDecimal price = stock.getQuote(true).getPrice();
		BigDecimal change = stock.getQuote().getChangeInPercent();
		BigDecimal eps = stock.getStats().getEps();
		BigDecimal dividend = stock.getDividend().getAnnualYieldPercent();
		
		stock.print();
		Object[] val = new Object[6];
		val[0] = sym;
		val[1] = name;
		val[2] = price;
		val[3] = change;
		val[4] = eps;
		val[5] = dividend;
		return val;
	}
	
	// Function sendStock. It sends the data that's retrieved from YahooFinance to the Firebase Firestore.
	public static boolean sendStock(Object[] sendVal) throws InterruptedException, ExecutionException {
		
		Firestore db = FirestoreClient.getFirestore();
		Map<Object, Object> Data = new HashMap<Object, Object>();
		DocumentReference docRef = db.collection("Stocks").document((String) sendVal[1]);
		Data.put("Symbol", sendVal[0]);
		Data.put("Name", sendVal[1]);
		Data.put("Market Price" , sendVal[2]);
		Data.put("Change", sendVal[3]);
		Data.put("EPS", sendVal[4]);
		Data.put("Dividend", sendVal[5]);
		ApiFuture<WriteResult> result = docRef.set(Data);
		System.out.println("Update Time : " + result.get().getUpdateTime());
		System.out.println("Done: " + result.isDone());
		return result.isDone();
	}

}
