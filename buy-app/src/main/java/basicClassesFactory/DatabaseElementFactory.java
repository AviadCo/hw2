package basicClassesFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import basicClasses.DatabaseElement;
import basicClasses.Order;
import databaseInterfaces.IStringableFactory;

/**
 * This class is Factory for DatabaseElement.
 * The class implements IStringableFactory factory
 * 
 * @author Aviad
 *
 */
public class DatabaseElementFactory implements IStringableFactory<DatabaseElement> {

	private final static String ELEMENT_SPLITER = ",";

	@Override
	public CompletableFuture<DatabaseElement> createObject(CompletableFuture<String> s) {
		return s.thenApply(strElement -> {
			List<Order> ordersList = new ArrayList<>();
			String[] elementString = strElement.split(ELEMENT_SPLITER);
					
			for (String order : Arrays.copyOfRange(elementString, 1, elementString.length)) {			
				ordersList.add(new Order(order));
			}
			
			return new DatabaseElement(elementString[0], ordersList);
		});
	}

	@Override
	public CompletableFuture<String> createString(CompletableFuture<DatabaseElement> e) {
		return e.thenApply(element -> {
			String elementString = element.getId();
			
			for (Order order : element.getOrdersList()) {
				elementString += ELEMENT_SPLITER + order.parseOrderToString();
			}
			
			return elementString;
		});
	}
	
}
