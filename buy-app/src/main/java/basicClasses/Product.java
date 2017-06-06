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
	Long price;
	
	public Product (String id, Long price) {
		this.id = id;
		this.price = price;
	}
	
	public Product (String id, String price) {
		this.id = id;
		this.price = Long.valueOf(price);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
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
