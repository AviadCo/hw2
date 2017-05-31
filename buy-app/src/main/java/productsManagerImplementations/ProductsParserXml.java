package productsManagerImplementations;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import basicClasses.DatabaseElement;
import basicClasses.Order;
import basicClasses.Product;

/**
 * This class parse the xml data to list of DatabaseElements.
 * 
 * @author Aviad
 *
 */
public class ProductsParserXml {

	/* Product tags */
	private static final String PRODUCT_TAG       = "Product";
	private static final String PRODUCT_ID_TAG    = "id";
	private static final String PRODUCT_PRICE_TAG = "price";
	
	/* Order tags */
	private static final String ORDER_TAG = "Order";
	private static final String ORDER_ID_TAG = "order-id";
	private static final String ORDER_USER_ID_TAG = "user-id";
	private static final String ORDER_PRODUCT_ID_TAG = "product-id";
	private static final String ORDER_AMOUNT_TAG = "amount";
	
	/* Cancel Order tags */
	private static final String CANCEL_ORDER_TAG = "CancelOrder";
	
	/* Modify Order tags */
	private static final String MODIFY_ORDER_TAG = "ModifyOrder";
	private static final String MODIFY_ORDER_NEW_AMOUNT_TAG = "new-amount";

	/**
	 * 
	 * @param xml - parse String xml to Document for DOM parser
	 * @return - Document of the xml
	 * @throws Exception - throws exception on DOM xml parser failure
	 */
	private static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    
	    return builder.parse(is);
	}
	
	/**
	 * 
	 * @param productNode - the xml node of the product
	 * @return - Product object
	 * @throws Exception - throws exception on DOM xml parser failure
	 */
	private static Product parseProductNode(Node productNode) throws Exception
	{
		Element productElement = (Element) productNode;
		
		Element idTag = (Element) productElement.getElementsByTagName(PRODUCT_ID_TAG).item(0);
		Element priceNode = (Element) productElement.getElementsByTagName(PRODUCT_PRICE_TAG).item(0);

		return new Product(idTag.getTextContent(), priceNode.getTextContent());
     }
	
	/**
	 * 
	 * @param xml - the xml string of the products and orders
	 * @return - list of products
	 * @throws Exception - throws exception on DOM xml parser failure
	 */
	public static List<Product> createListOfProducts(String xml) throws Exception
	{
		Map<String, Product> productMap = new HashMap<String, Product> ();
		
		Document doc = loadXMLFromString(xml);
		
		doc.getDocumentElement().normalize();
        
        NodeList nList = doc.getElementsByTagName(PRODUCT_TAG);
        
        for (int currentProductIndex = 0; currentProductIndex < nList.getLength(); currentProductIndex++) {
            Node currentProductNode = nList.item(currentProductIndex);
            
            if (currentProductNode.getNodeType() == Node.ELEMENT_NODE) {
            	Product currentProduct = parseProductNode(currentProductNode);
            	
            	productMap.put(currentProduct.getKey(), currentProduct);
            }
         }
	    
	    return new ArrayList<>(productMap.values());
	}
	
	/**
	 * 
	 * @param orderNode - the xml node of the order
	 * @return - Order object
	 * @throws Exception - throws exception on DOM xml parser failure
	 */
	private static Order parseOrderNode(Node orderNode) throws Exception
	{
		Element orderElement = (Element) orderNode;
		
		Element idTag = (Element) orderElement.getElementsByTagName(ORDER_ID_TAG).item(0);
		Element userTag = (Element) orderElement.getElementsByTagName(ORDER_USER_ID_TAG).item(0);
		Element productTag = (Element) orderElement.getElementsByTagName(ORDER_PRODUCT_ID_TAG).item(0);
		Element amountTag = (Element) orderElement.getElementsByTagName(ORDER_AMOUNT_TAG).item(0);
		
		return new Order(idTag.getTextContent(), userTag.getTextContent(),
						 productTag.getTextContent(), amountTag.getTextContent(),
						 Order.COMMIT_ORDER_TYPE);
     }
	
	/**
	 * 
	 * @param orderNode - the xml node of the order
	 * @return - Order object
	 * @throws Exception - throws exception on DOM xml parser failure
	 */
	private static Order parseCancelOrderNode(Node orderNode) throws Exception
	{
		Element orderElement = (Element) orderNode;
		
		Element idTag = (Element) orderElement.getElementsByTagName(ORDER_ID_TAG).item(0);
		
		return new Order(idTag.getTextContent(), "0", "0", "0", Order.CANCEL_ORDER_TYPE);
     }
	
	/**
	 * 
	 * @param orderNode - the xml node of the order
	 * @return - Order object
	 * @throws Exception - throws exception on DOM xml parser failure
	 */
	private static Order parseModifyOrderNode(Node orderNode) throws Exception
	{
		Element orderElement = (Element) orderNode;
		
		Element idTag = (Element) orderElement.getElementsByTagName(ORDER_ID_TAG).item(0);
		Element newAmountTag = (Element) orderElement.getElementsByTagName(MODIFY_ORDER_NEW_AMOUNT_TAG).item(0);
		
		return new Order(idTag.getTextContent(), "0", "0", newAmountTag.getTextContent(), Order.MODIFY_ORDER_TYPE);
     }
	
	/**
	 * 
	 * @param xml - the xml string of the products and orders
	 * @return - list of products
	 * @throws Exception - throws exception on DOM xml parser failure
	 */
	public static List<DatabaseElement> createListOfOrders(String xml) throws Exception
	{
		Map<String, DatabaseElement> ordersMap = new HashMap<String, DatabaseElement> ();
		
		Document doc = loadXMLFromString(xml);
		
		doc.getDocumentElement().normalize();
        
        NodeList nList = doc.getElementsByTagName("*");
        
        for (int currentElementIndex = 0; currentElementIndex < nList.getLength(); currentElementIndex++) {
            Node currentElementNode = nList.item(currentElementIndex);
            
            if (currentElementNode.getNodeType() == Node.ELEMENT_NODE) {
            	Order orderOperation;
            	
            	if (currentElementNode.getNodeName().equals(ORDER_TAG)) {
            		orderOperation = parseOrderNode(currentElementNode);
            	} else if (currentElementNode.getNodeName().equals(CANCEL_ORDER_TAG)) {
            		orderOperation = parseCancelOrderNode(currentElementNode);
            	} else if (currentElementNode.getNodeName().equals(MODIFY_ORDER_TAG)) {
            		orderOperation = parseModifyOrderNode(currentElementNode);
            	} else {
            		continue;
            	}
            	            	
            	if (ordersMap.containsKey(orderOperation.getOrderID())) {
            		DatabaseElement sameOrder = ordersMap.get(orderOperation.getOrderID());
            		
            		List<Order> allOrderOperations = new ArrayList<>(sameOrder.getOrdersList());
            		allOrderOperations.add(orderOperation);
            		
            		ordersMap.put(sameOrder.getKey(), new DatabaseElement(sameOrder.getKey(), allOrderOperations));
            	} else {          		
            		List<Order> orderOperations = new ArrayList<>();
            		orderOperations.add(orderOperation);
            		
            		ordersMap.put(orderOperation.getOrderID(), new DatabaseElement(orderOperation.getOrderID(), orderOperations));
                }
            }
         }
	    
	    return new ArrayList<>(ordersMap.values());
	}
}
