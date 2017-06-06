package productsManagerImplementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import basicClasses.DatabaseElement;
import basicClasses.Order;
import basicClasses.Product;

/**
 * This class parse the xml data to list of DatabaseElements.
 * 
 * @author Aviad
 *
 */
public class ProductsParserJson {
	
	private static final String TYPE_TAG = "type";
	
	/* Product tags */
	private static final String PRODUCT_TAG       = "product";
	private static final String PRODUCT_ID_TAG    = "id";
	private static final String PRODUCT_PRICE_TAG = "price";
	
	/* Order tags */
	private static final String ORDER_TAG            = "order";
	private static final String ORDER_ID_TAG 		 = "order-id";
	private static final String ORDER_USER_ID_TAG    = "user-id";
	private static final String ORDER_PRODUCT_ID_TAG = "product-id";
	private static final String ORDER_AMOUNT_TAG     = "amount";
	
	/* Cancel Order tags */
	private static final String CANCEL_ORDER_TAG = "cancel-order";
	
	/* Modify Order tags */
	private static final String MODIFY_ORDER_TAG = "modify-order";
	
	/**
	 * 
	 * @param xml - the json string of the products and orders
	 * @return - list of products
	 */
	@SuppressWarnings("unchecked")
	public static List<Product> createListOfProducts(String jsonData)
	{
		List<Product> products = new ArrayList<Product>();
		
		try {
			((JSONArray) new JSONParser().parse(jsonData)).forEach(item -> {
			    final JSONObject obj = (JSONObject) item;
			    final String type    = (String) obj.get(TYPE_TAG);
			    
			    if (type.equals(PRODUCT_TAG)) {
				    final String id    = (String) obj.get(PRODUCT_ID_TAG);
				    final Long price = (Long) obj.get(PRODUCT_PRICE_TAG);
				    
				    products.add(new Product (id, price));
			    }
			});
		} catch (ParseException e) {
			throw new RuntimeException();
		}
		
	    return products;
	}

	private static Optional<Order> parseOrder(final JSONObject obj)
	{
	    String type    = (String) obj.get(TYPE_TAG);
		
	    if (type.equals(ORDER_TAG)) {
		    String id    = (String) obj.get(ORDER_ID_TAG);
		    String user_id = (String) obj.get(ORDER_USER_ID_TAG);
		    String product_id = (String) obj.get(ORDER_PRODUCT_ID_TAG);
	    	Integer amount = ((Long) obj.get(ORDER_AMOUNT_TAG)).intValue();
		    
		    return Optional.of(new Order(id, user_id, product_id, amount, Order.COMMIT_ORDER_TYPE));
	    } else if (type.equals(CANCEL_ORDER_TAG)) {
	    	String id    = (String) obj.get(ORDER_ID_TAG);
	    	
	    	return Optional.of(new Order(id, "0", "0", "0", Order.CANCEL_ORDER_TYPE));
	    } else if (type.equals(MODIFY_ORDER_TAG)) {
	    	String id    = (String) obj.get(ORDER_ID_TAG);
	    	Integer amount = ((Long) obj.get(ORDER_AMOUNT_TAG)).intValue();
		    
		    return Optional.of(new Order(id, "0", "0", amount, Order.MODIFY_ORDER_TYPE));
	    } else {
	    	return Optional.empty();
	    }
	}
	
	/**
	 * 
	 * @param xml - the json string of the products and orders
	 * @return - list of DatabaseElement (list od order operations)
	 */
	@SuppressWarnings("unchecked")
	public static List<DatabaseElement> createListOfOrders(String jsonData)
	{
		Map<String, DatabaseElement> ordersMap = new HashMap<String, DatabaseElement>();
		
		try {
			((JSONArray) new JSONParser().parse(jsonData)).forEach(item -> {
			    DatabaseElement element;
			    Optional<Order> order = parseOrder((JSONObject) item);	    
			   
			    if (order.isPresent()) {
				    /* adding order to map */
				    element = ordersMap.containsKey(order.get().getOrderID()) ? ordersMap.get(order.get().getOrderID()) :
				    													  new DatabaseElement(order.get().getOrderID());
			    	element.getOrdersList().add(order.get());
			    	ordersMap.put(order.get().getOrderID(), element);
			    }
			});
		} catch (ParseException e) {
			throw new RuntimeException();
		}
		
	    return new ArrayList<DatabaseElement>(ordersMap.values());
	}
}