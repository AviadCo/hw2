package basicClasses;

import java.util.ArrayList;
import java.util.List;

import databaseInterfaces.IDatabaseElement;

/**
 * This class implements IDatabaseElement interface.
 * 
 * @author Aviad
 *
 */
public class DatabaseElement implements IDatabaseElement<String, DatabaseElement> {

	private String id;
	private List<Order> ordersList;
	
	/**
	 * @param id - id of the element
	 */
	public DatabaseElement (String id) {
		this.id = id;
		this.ordersList = new ArrayList<>();
	}
	
	/**
	 * 
	 * @param id - id of the element
	 * @param ordersList - list of orders which belongs to element
	 */
	public DatabaseElement (String id, List<Order> ordersList) {
		this.id = id;
		this.ordersList = ordersList;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public List<Order> getOrdersList() {
		return ordersList;
	}

	public void setOrdersList(List<Order> ordersList) {
		this.ordersList = ordersList;
	}

	@Override
	public String getKey() {
		return id;
	}

	@Override
	public DatabaseElement getValue() {
		return this;
	}

	/**
	 * 
	 * @param review - review to add to the element list
	 */
	public void addReview(Order order) {
		ordersList.add(order);
	}
}
