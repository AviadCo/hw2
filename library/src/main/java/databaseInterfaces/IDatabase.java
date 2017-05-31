package databaseInterfaces;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This is interface represent database storage for elements.
 * The elements must implement IDatabaseElement interface.
 * 
 * @author Aviad
 *
 */
public interface IDatabase<Key extends Comparable<Key>, Value> {	
	/**
	 * @return - number of elements in database
	 */
	CompletableFuture<Integer> getNumberOfElements();

	/**
	 * Add multiple elements at once
	 * 
	 * Do not add multiple elements more than once since the database sorting won't be maintained
	 * 
	 * @param elements - list of elements to add
	 */
	void add(List<? extends IDatabaseElement<Key, Value>> elements);

	/**
	 * findStudentByID - returns element by it's key using binary search algorithm.
	 * 
	 * If element dosen't exist returns Optional.empty().
	 *  */
	CompletableFuture<Optional<Value>> findElementByID(Key key);
}
