package databaseImplementations;

import databaseInterfaces.IDatabaseElement;

/**
 * This class implements IDatabaseElement interface for testing Database class
 * 
 * @author Aviad
 *
 */
public class DatabaseElementTester implements IDatabaseElement<String, String> {

	private String id;
	private String value;

	/**
	 * 
	 * @param id - id of the element
	 * @param value - value of the element
	 */
	public DatabaseElementTester (String id, String value) {
		this.id = id;
		this.value = value;
	}

	@Override
	public String getKey() {
		return id;
	}

	@Override
	public String getValue() {
		return value;
	}
}
