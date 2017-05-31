package basicClasses;

/**
 * This class represent an order.
 * 
 * @author Aviad
 *
 */
public class Order {

	private final static String ORDER_SPLITER = " ";
	
	private String orderID;
	private String clientID;
	private String productID;
	private Integer numOfProducts;
	
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
	}
	
	public Order (String orderID, String clientID, String productID, String numOfProducts) {
		this.orderID = orderID;
		this.clientID = clientID;
		this.productID = productID;
		this.numOfProducts = Integer.valueOf(numOfProducts);
	}
	
	public Order (String orderID, String clientID, String productID, Integer numOfProducts) {
		this.orderID = orderID;
		this.clientID = clientID;
		this.productID = productID;
		this.numOfProducts = numOfProducts;
	}
		
	/**
	 * 
	 * @return string which represents the order
	 */
	public String parseOrderToString() {
		return orderID + ORDER_SPLITER + clientID + ORDER_SPLITER + productID + ORDER_SPLITER + numOfProducts.toString();
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
}
