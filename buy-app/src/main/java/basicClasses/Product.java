package basicClasses;

import databaseInterfaces.IDatabaseElement;

/**
 * This class represents product to be stores in database.
 * 
 * @author Aviad
 *
 */

public class Product implements IDatabaseElement<String, Product> {

	String id;
	Integer price;
	
	public Product (String id, Integer price) {
		this.id = id;
		this.price = price;
	}
	
	public Product (String id, String price) {
		this.id = id;
		this.price = Integer.valueOf(price);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	@Override
	public String getKey() {
		return id;
	}

	@Override
	public Product getValue() {
		return this;
	}
}
