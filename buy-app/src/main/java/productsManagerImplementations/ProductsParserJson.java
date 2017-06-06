package productsManagerImplementations;

import java.util.List;

import basicClasses.DatabaseElement;
import basicClasses.Product;

/**
 * This class parse the xml data to list of DatabaseElements.
 * 
 * @author Aviad
 *
 */
public class ProductsParserJson {
	
	/**
	 * 
	 * @param xml - the json string of the products and orders
	 * @return - list of products
	 */
	public static List<Product> createListOfProducts(String jsonData)
	{
	    return null;
	}

	/**
	 * 
	 * @param xml - the json string of the products and orders
	 * @return - list of DatabaseElement (list od order operations)
	 */
	public static List<DatabaseElement> createListOfOrders(String jsonData)
	{
		return null;
	}
}