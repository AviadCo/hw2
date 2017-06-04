package databaseInterfaces;

import java.util.concurrent.CompletableFuture;

/**
 * This interface represents a factory of element.
 * The factory is able to create object from string and the string to object.
 * 
 * @author Aviad
 *
 * @param <ElementType> - The element type
 */
public interface IStringableFactory<ElementType> {
	/**
	 * 
	 * @param s  - String which represents the object form type ElementType
	 * @return - CompletableFuture of the element from type ElementType
	 */
	CompletableFuture<ElementType> createObject(CompletableFuture<String> s);
	
	/**
	 * 
	 * @param e  - the element from type ElementType
	 * @return - CompletableFuture of String which represents the object form type ElementType
	 */
	CompletableFuture<String> createString(CompletableFuture<ElementType> e);
}
