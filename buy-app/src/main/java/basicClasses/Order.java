package basicClasses;

/**
 * This class represent an order.
 * 
 * @author Aviad
 *
 */
public class Order {

	public static final String COMMIT_ORDER_TYPE = "COMMIT_ORDER";
	public static final String MODIFY_ORDER_TYPE = "MODIFY_ORDER";
	public static final String CANCEL_ORDER_TYPE = "CANCEL_ORDER";
		
	private final static String ORDER_SPLITER = " ";
	
	private String orderID;
	private String clientID;
	private String productID;
	private Integer numOfProducts;
	private String type;
	
	/**
	 * 
	 * @param order - order in String ("orderID" "clientID" "productID" "numOfProducts")
	 */
	public Order (String order) {
		String[] orderArray = order.split(ORDER_SPLITER);
		
		orderID = orderArray[0];
		clientID = orderArray[1];
		productID = orderArray[2];
		numOfProducts = Integer.valueOf(orderArray[3]);
		type = orderArray[4];
	}
	
	public Order (String orderID, String clientID, String productID, String numOfProducts, String type) {
		this.orderID = orderID;
		this.clientID = clientID;
		this.productID = productID;
		this.numOfProducts = Integer.valueOf(numOfProducts);
		this.type = type;
	}
	
	public Order (String orderID, String clientID, String productID, Integer numOfProducts, String type) {
		this.orderID = orderID;
		this.clientID = clientID;
		this.productID = productID;
		this.numOfProducts = numOfProducts;
		this.type = type;
	}
		
	/**
	 * 
	 * @return string which represents the order
	 */
	public String parseOrderToString() {
		return orderID + ORDER_SPLITER + clientID + ORDER_SPLITER + productID + ORDER_SPLITER + numOfProducts.toString() + ORDER_SPLITER + type;
	}
	
	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public Integer getNumOfProducts() {
		return numOfProducts;
	}

	public void setNumOfProducts(Integer numOfProducts) {
		this.numOfProducts = numOfProducts;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
