package databaseImplementations;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

public class DatabaseTest {
	
	private static List<DatabaseElementTester> elementsList;
	private static Database<String, String> database;
	
	private static final String DATABASE_NAME_TEST = "DATABASE_TEST";
	private static final String KEYS = "KEYS";
	private static final String VALUES = "VALUES";
	
	public static final String ELEMENT_KEY = "ELEMENT_KEY";
	public static final String ELEMENT_VALUE = "ELEMENT_VALUE";
	public static final String INVALID_KEY = "INVALID_KEY";
	
	@BeforeClass
	public static void setup() {	
	    try {
			database = new Database<String, String>(new MapBasedStorageFactory().open(DatabaseTest.DATABASE_NAME_TEST + KEYS).get(),
													new MapBasedStorageFactory().open(DatabaseTest.DATABASE_NAME_TEST + VALUES).get(),
													new StringFactoryTester(), new StringFactoryTester());
		} catch (IndexOutOfBoundsException | InterruptedException | ExecutionException e) {
			fail();
		}
		
		elementsList = new ArrayList<DatabaseElementTester>();
		Map<String, DatabaseElementTester>  elementsMap = new HashMap<String, DatabaseElementTester>(); /* we don't want duplicated keys */
		
		/* Add random keys and values to check sorting */
		for (Integer i = 0; i < 10; ++i) {
			Integer key = (int) (Math.random() * 10000);
			Integer value = (int) (Math.random() * 10000);
			
			elementsMap.put(ELEMENT_KEY + key.toString(), new DatabaseElementTester(ELEMENT_KEY + key.toString(), ELEMENT_VALUE + value.toString()));
		}
		
		elementsList.addAll(elementsMap.values());
		
		database.add(elementsList);
	}
	
	@Test (timeout = 500)
	public void getNumberOfElementsTest()
	{
		try {
			assertEquals((Integer)elementsList.size(), (Integer) database.getNumberOfElements().get().intValue());
		} catch (InterruptedException | ExecutionException e) {
			fail();
		}
	}
	
	@Test (timeout = 500)
	public void checkMissingKeyTest()
	{
		try {
			assertEquals(database.findElementByID(INVALID_KEY).get(), Optional.empty());
		} catch (InterruptedException | ExecutionException e) {
			fail();
		}
	}
	
	@Test (timeout = 20000)
	public void findElementByIDTest()
	{
		for (DatabaseElementTester element : elementsList) {
			try {
				assertEquals(database.findElementByID(element.getKey()).get(), Optional.of(element.getValue()));
			} catch (InterruptedException | ExecutionException e) {
				fail();
			}
		}
	}
}
