package productManagerTest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.inject.Guice;
import com.google.inject.Injector;

import il.ac.technion.cs.sd.buy.app.BuyProductInitializer;
import il.ac.technion.cs.sd.buy.app.BuyProductReader;
import il.ac.technion.cs.sd.buy.test.BuyProductModule;

public class ProductPackageTest {
	
	@Rule public Timeout globalTimeout = Timeout.seconds(20);

	  private static Injector setupAndGetInjector(String fileName) throws FileNotFoundException {
		    String fileContents =
		        new Scanner(new File(ProductPackageTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
		    Injector injector = Guice.createInjector(new BuyProductModule());//, new LineStorageModule());
		    BuyProductInitializer bpi = injector.getInstance(BuyProductInitializer.class);
		    if (fileName.endsWith("xml"))
		      bpi.setupXml(fileContents);
		    else {
		    	//TODO add Json tests
		      //assert fileName.endsWith("json");
		      //bpi.setupJson(fileContents);
		    }
		    return injector;
		  }

	  @Test
	  public void simpleXmlTest() throws Exception {
		    Injector injector = setupAndGetInjector("simpleTest.xml");
	
		    BuyProductReader reader = injector.getInstance(BuyProductReader.class);
		    assertEquals(Arrays.asList(5, 10, -1), reader.getHistoryOfOrder("1").get());
	  }
	  
	  @Test
	  public void productRedefintionAndNotExistsTest() throws Exception {
		    Injector injector = setupAndGetInjector("productRedefintionAndNotExists.xml");
		
			BuyProductReader reader = injector.getInstance(BuyProductReader.class);
			
			assertFalse(reader.isValidOrderId("3").get()); // product id doesn't exist
			assertTrue(reader.isValidOrderId("1").get()); // product valid
			assertTrue(reader.isValidOrderId("2").get()); // product valid
			assertTrue(reader.isValidOrderId("4").get()); // product valid
			
			assertEquals(Long.valueOf(50), reader.getTotalAmountSpentByUser("1").get()); // Bisly cost 5 with amount 10 - should be 10 * 5
			assertEquals(Long.valueOf(50), reader.getTotalAmountSpentByUser("2").get()); // Bamba redifined - should be 10 * 5
	  }
	
	  @Test
	  public void orderModifiedCanceledTest() throws Exception {
		    Injector injector = setupAndGetInjector("orderModifiedCanceledTest.xml");
		
			BuyProductReader reader = injector.getInstance(BuyProductReader.class);
						
			/* Modified checks */
			assertTrue(reader.isModifiedOrder("ModifiedAndCanceled").get());
			assertTrue(reader.isModifiedOrder("OnlyModified").get());
			assertFalse(reader.isModifiedOrder("OnlyCanceled").get());
			assertFalse(reader.isModifiedOrder("RegularOrder").get());
			assertFalse(reader.isModifiedOrder("ModifiedAndCanceledAndRedefined").get());
			assertTrue(reader.isModifiedOrder("RedefinedAndModifiedTwice").get());
			assertFalse(reader.isModifiedOrder("NoExists").get());
			
			/* Canceled checks */
			assertTrue(reader.isCanceledOrder("ModifiedAndCanceled").get());
			assertFalse(reader.isCanceledOrder("OnlyModified").get());
			assertTrue(reader.isCanceledOrder("OnlyCanceled").get());
			assertFalse(reader.isCanceledOrder("RegularOrder").get());
			assertFalse(reader.isCanceledOrder("ModifiedAndCanceledAndRedefined").get());
			assertFalse(reader.isCanceledOrder("RedefinedAndModifiedTwice").get());
			assertFalse(reader.isModifiedOrder("NoExists").get());
	  }
	  
	  @Test
	  public void getNumOfProductsAndHistoryTest() throws Exception {
		    Injector injector = setupAndGetInjector("getNumOfProductsAndHistory.xml");
		
			BuyProductReader reader = injector.getInstance(BuyProductReader.class);
						
			/* getNumberOfProductOrdered checks */
			assertEquals(Integer.valueOf(-10),
						 Integer.valueOf(reader.getNumberOfProductOrdered("ModifiedAndCanceled").get().getAsInt()));
			assertEquals(Integer.valueOf(6),
					 Integer.valueOf(reader.getNumberOfProductOrdered("OnlyModified").get().getAsInt()));
			assertEquals(Integer.valueOf(-5),
					 Integer.valueOf(reader.getNumberOfProductOrdered("OnlyCanceled").get().getAsInt()));
			assertEquals(Integer.valueOf(5),
					 Integer.valueOf(reader.getNumberOfProductOrdered("RegularOrder").get().getAsInt()));
			assertEquals(Integer.valueOf(50),
					 Integer.valueOf(reader.getNumberOfProductOrdered("ModifiedAndCanceledAndRedefined").get().getAsInt()));
			assertEquals(Integer.valueOf(30),
					 Integer.valueOf(reader.getNumberOfProductOrdered("RedefinedAndModifiedTwice").get().getAsInt()));
			assertEquals(Integer.valueOf(100),
					 Integer.valueOf(reader.getNumberOfProductOrdered("MultipleModification").get().getAsInt()));
			assertEquals(OptionalInt.empty(), reader.getNumberOfProductOrdered("NoExists").get());
			
			/* getHistoryOfOrder checks */
		    assertEquals(Arrays.asList(5, 10, -1), reader.getHistoryOfOrder("ModifiedAndCanceled").get());
		    assertEquals(Arrays.asList(5, 6), reader.getHistoryOfOrder("OnlyModified").get());
		    assertEquals(Arrays.asList(5, -1), reader.getHistoryOfOrder("OnlyCanceled").get());
		    assertEquals(Arrays.asList(5), reader.getHistoryOfOrder("RegularOrder").get());
		    assertEquals(Arrays.asList(50), reader.getHistoryOfOrder("ModifiedAndCanceledAndRedefined").get());
		    assertEquals(Arrays.asList(50, 30), reader.getHistoryOfOrder("RedefinedAndModifiedTwice").get());
		    assertEquals(Arrays.asList(50, 20, 10, -1, 100), reader.getHistoryOfOrder("MultipleModification").get());
		    assertEquals(Arrays.asList(), reader.getHistoryOfOrder("NoExists").get());
	  }
	  
	  @Test
	  public void getOrderIdsandGetTotalAmoutByUserTest() throws Exception {
		    Injector injector = setupAndGetInjector("getOrderIdsandGetTotalAmoutByUser.xml");
		
			BuyProductReader reader = injector.getInstance(BuyProductReader.class);
						
			/* getOrderIdsForUser checks */
		    assertEquals(Arrays.asList("ModifiedAndCanceled"), reader.getOrderIdsForUser("1").get());
		    assertEquals(Arrays.asList("OnlyModified"), reader.getOrderIdsForUser("2").get());
		    assertEquals(Arrays.asList("OnlyCanceled"), reader.getOrderIdsForUser("3").get());
		    assertEquals(Arrays.asList("RegularOrder"), reader.getOrderIdsForUser("4").get());
		    assertEquals(Arrays.asList("ModifiedAndCanceledAndRedefined", "MultipleModification", "RedefinedAndModifiedTwice"),
		    			 reader.getOrderIdsForUser("5").get());
		
			/* getTotalAmountSpentByUser checks */
		    assertEquals(Long.valueOf(0), reader.getTotalAmountSpentByUser("1").get());
		    assertEquals(Long.valueOf(6 * 10), reader.getTotalAmountSpentByUser("2").get());
		    assertEquals(Long.valueOf(0), reader.getTotalAmountSpentByUser("3").get());
		    assertEquals(Long.valueOf(5 * 500), reader.getTotalAmountSpentByUser("4").get());
		    assertEquals(Long.valueOf(50 * 10 + 30 * 10 + 100 * 20), reader.getTotalAmountSpentByUser("5").get());
	  }
	  
	  @Test
	  public void getUsersAndOrderByProductsTest() throws Exception {
		    Injector injector = setupAndGetInjector("getUsersAndOrderByProducts.xml");
		
			BuyProductReader reader = injector.getInstance(BuyProductReader.class);
						
			/* getUsersThatPurchased checks */
		    assertEquals(Arrays.asList(), reader.getUsersThatPurchased("Bamba").get());
		    assertEquals(Arrays.asList("2", "5"), reader.getUsersThatPurchased("Bisly").get());
		    assertEquals(Arrays.asList("5"), reader.getUsersThatPurchased("Krembo").get());
		    assertEquals(Arrays.asList("4"), reader.getUsersThatPurchased("Cola").get());
		    assertEquals(Arrays.asList(), reader.getUsersThatPurchased("NotExists").get());
		
			/* getOrderIdsThatPurchased checks */
		    assertEquals(Arrays.asList("ModifiedAndCanceled"), reader.getOrderIdsThatPurchased("Bamba").get());
		    assertEquals(Arrays.asList("ModifiedAndCanceledAndRedefined", "OnlyModified", "RedefinedAndModifiedTwice"),
		    			 reader.getOrderIdsThatPurchased("Bisly").get());
		    assertEquals(Arrays.asList("MultipleModification"), reader.getOrderIdsThatPurchased("Krembo").get());
		    assertEquals(Arrays.asList("OnlyCanceled", "RegularOrder"), reader.getOrderIdsThatPurchased("Cola").get());
		    assertEquals(Arrays.asList(), reader.getOrderIdsThatPurchased("NotExists").get());
	  }
	  	  
	  @Test
	  public void getCancelModifyRatioTest() throws Exception {
		    Injector injector = setupAndGetInjector("getCancelModifyRatio.xml");
		
			BuyProductReader reader = injector.getInstance(BuyProductReader.class);
						
			/* getCancelRatioForUser checks */
		    assertEquals(Double.valueOf(1 / 1), Double.valueOf(reader.getCancelRatioForUser("1").get().getAsDouble()));
		    assertEquals(Double.valueOf((double) 0 / 1), Double.valueOf(reader.getCancelRatioForUser("2").get().getAsDouble()));
		    assertEquals(Double.valueOf(1 / 1), Double.valueOf(reader.getCancelRatioForUser("3").get().getAsDouble()));
		    assertEquals(Double.valueOf(0 / 1), Double.valueOf(reader.getCancelRatioForUser("4").get().getAsDouble()));
		    assertEquals(Double.valueOf(0 / 3), Double.valueOf(reader.getCancelRatioForUser("5").get().getAsDouble()));
		    assertEquals(OptionalDouble.empty(), reader.getCancelRatioForUser("NotExists").get());
		
			/* getModifyRatioForUser checks */
		    assertEquals(Double.valueOf(1 / 1), Double.valueOf(reader.getModifyRatioForUser("1").get().getAsDouble()));
		    assertEquals(Double.valueOf((double) 1 / 1), Double.valueOf(reader.getModifyRatioForUser("2").get().getAsDouble()));
		    assertEquals(Double.valueOf(1 / 1), Double.valueOf(reader.getModifyRatioForUser("3").get().getAsDouble()));
		    assertEquals(Double.valueOf(0 / 1), Double.valueOf(reader.getModifyRatioForUser("4").get().getAsDouble()));
		    assertEquals(Double.valueOf((double) 2 / 3), Double.valueOf(reader.getModifyRatioForUser("5").get().getAsDouble()));
		    assertEquals(OptionalDouble.empty(), reader.getModifyRatioForUser("NotExists").get());
	  }
	 
	  @Test
	  public void getItemsPurchasedTest() throws Exception {
		    Injector injector = setupAndGetInjector("getItemsPurchased.xml");
		
			BuyProductReader reader = injector.getInstance(BuyProductReader.class);
						
			/* getItemsPurchasedByUsers checks */
		    assertEquals(new HashMap<String, Long>(), reader.getItemsPurchasedByUsers("Bamba").get());
		    
		    Map<String, Long> mapBisly = new HashMap<String, Long>();
		    mapBisly.put("2", (long) 6);
		    mapBisly.put("5", (long) 50 + 30);
		    assertEquals(mapBisly, reader.getItemsPurchasedByUsers("Bisly").get());
		    
		    Map<String, Long> mapKrembo = new HashMap<String, Long>();
		    mapKrembo.put("5", (long) 100);
		    assertEquals(mapKrembo, reader.getItemsPurchasedByUsers("Krembo").get());
		    
		    Map<String, Long> mapCola = new HashMap<String, Long>();
		    mapCola.put("4", (long) 5);
		    assertEquals(mapCola, reader.getItemsPurchasedByUsers("Cola").get());
		    
		    assertEquals(new HashMap<String, Long>(), reader.getItemsPurchasedByUsers("NotExists").get());
		
			/* getAllItemsPurchased checks */
		    assertEquals(new HashMap<String, Long>(), reader.getAllItemsPurchased("1").get());
		    
		    Map<String, Long> mapUser2 = new HashMap<String, Long>();
		    mapUser2.put("Bisly", (long) 6);
		    assertEquals(mapUser2, reader.getAllItemsPurchased("2").get());
		    
		    assertEquals(new HashMap<String, Long>(), reader.getAllItemsPurchased("3").get());
		    
		    Map<String, Long> mapUser4 = new HashMap<String, Long>();
		    mapUser4.put("Cola", (long) 5);
		    assertEquals(mapUser4, reader.getAllItemsPurchased("4").get());
		    
		    Map<String, Long> mapUser5 = new HashMap<String, Long>();
		    mapUser5.put("Bisly", (long) 50  + 30);
		    mapUser5.put("Krembo", (long) 100);
		    assertEquals(mapUser5, reader.getAllItemsPurchased("5").get());
		    
		    assertEquals(new HashMap<String, Long>(), reader.getAllItemsPurchased("NotExists").get());
	  }
}
