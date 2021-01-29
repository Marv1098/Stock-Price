import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockPriceTest {
	
	// Change stock symbol as needed.
	String testSym = "tsla";
	
	@Test
	public void testGetStock() throws IOException, InterruptedException, ExecutionException {
		
		Object[] valTest = getStockTest(testSym);
		Object[] stock = StockPrice.getStock(testSym);
		assertArrayEquals(valTest, stock);
	}
	
	@Test
	public void testSendStock() throws IOException, InterruptedException, ExecutionException {
		
		Object[] valTest = getStockTest(testSym);
		Object[] stock = StockPrice.getStock(testSym);
		assertEquals(sendStockTest(valTest), StockPrice.sendStock(stock));
	}
	
	// Unit Testing function for getting stock data from YahooFinance
	private Object[] getStockTest(String testSym) throws IOException {
		Stock stockTest = YahooFinance.get(testSym, true);
		
		String symbol = stockTest.getSymbol();
		String name = stockTest.getName();
		BigDecimal price = stockTest.getQuote(true).getPrice();
		BigDecimal change = stockTest.getQuote().getChangeInPercent();
		BigDecimal eps = stockTest.getStats().getEps();
		BigDecimal dividend = stockTest.getDividend().getAnnualYieldPercent();
		
		Object[] valTest = new Object[6];
		valTest[0] = symbol;
		valTest[1] = name;
		valTest[2] = price;
		valTest[3] = change;
		valTest[4] = eps;
		valTest[5] = dividend;
		return valTest;
	}
	
	// Unit Testing function for sending stock data to Firebase Cloud Firestore.
	private boolean sendStockTest(Object[] sendValTest) throws InterruptedException, ExecutionException, IOException {
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
		String projectId = "stockprice-200d6";
		FirebaseOptions builder = FirebaseOptions.builder()
				.setCredentials(credentials)
				.setProjectId(projectId)
				.build();
		FirebaseApp.initializeApp(builder);
		
		Firestore db = FirestoreClient.getFirestore();
		Map<Object, Object> Data = new HashMap<Object, Object>();
		ApiFuture<WriteResult> result = null;
				
		DocumentReference docRef = db.collection("Stocks").document((String) sendValTest[1]);
		Data.put("Symbol", sendValTest[0]);
		Data.put("Name", sendValTest[1]);
		Data.put("Market Price" , sendValTest[2]);
		Data.put("Change", sendValTest[3]);
		Data.put("EPS", sendValTest[4]);
		Data.put("Dividend", sendValTest[5]);
		result = docRef.set(Data);
		System.out.println("Update Time -> " + result.get().getUpdateTime());
		System.out.println("Done -> " + result.isDone());
		return result.isDone();
	}

}
