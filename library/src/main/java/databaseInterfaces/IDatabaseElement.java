package databaseInterfaces;

/**
 * This is interface represent element to be stored in database.
 * The element is pair of key-value.
 * The key must implement Comparable interface
 * 
 * @author Aviad
 *
 */
public interface IDatabaseElement<Key extends Comparable<Key>, Value> {
	/**
	 * @return - the key of the element.
	 */
	Key getKey();
	
	/**
	 * @return - the value of the element.
	 */
	Value getValue();
}