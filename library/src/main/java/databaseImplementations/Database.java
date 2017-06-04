package databaseImplementations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;

import com.google.inject.Inject;

import databaseInterfaces.IDatabase;
import databaseInterfaces.IDatabaseElement;
import databaseInterfaces.IStringableFactory;

/**
 * This class represents a database implementation.
 * The elements in the database must consist of Key and Value pair. 
 * 
 * @author Aviad
 *
 * @param <Key> - The type of element key
 * @param <Value> - The type of element value.
 */
public class Database<Key extends Comparable<Key>, Value> implements IDatabase<Key, Value> {

	FutureLineStorage lineStorageKeys;
	FutureLineStorage lineStorageValues;
	IStringableFactory<Key> keyFactory;
	IStringableFactory<Value> valueFactory;
	
	static private final Integer SEARCH_OUT_OF_BOUND    = -1;
	static private final Integer SEARCH_LAST_INTERATION = 0;
	static private final Integer SEARCH_CAN_CONTINUE    = 1;

	/**
	 * 
	 * @param lineStorageKeys - lineStorage to store the keys
	 * @param lineStorageValues - lineStorage to store the values
	 * @param keyFactory - factory to create key from string
	 * @param valueFactory - factory to create value from string
	 */
	@Inject
	public Database(FutureLineStorage lineStorageKeys, FutureLineStorage lineStorageValues,
					IStringableFactory<Key> keyFactory, IStringableFactory<Value> valueFactory) {
		this.lineStorageKeys = lineStorageKeys;
		this.lineStorageValues = lineStorageValues;
		this.keyFactory = keyFactory;
		this.valueFactory = valueFactory;
	}
	
	/**
	 * 
	 * @param index - line index of the value
	 * @return - Value that is stored in the row index
	 */
	private CompletableFuture<Value> getValueByIndex(Integer index) {
		CompletableFuture<String> lineValue = lineStorageValues.read(index);
		
		return valueFactory.createObject(lineValue);
	}
	
	/**
	 * 
	 * @param index - line index of the key 
	 * @return - key that is stored in the row index
	 */
	private CompletableFuture<Key> getKeyByIndex(Integer index) {
		return keyFactory.createObject(lineStorageKeys.read(index));
	}
	
	/**
	 * @return - number of elements in database
	 */
	@Override
	public CompletableFuture<Integer> getNumberOfElements() {
		return lineStorageValues.numberOfLines();
	}

	/**
	 * 
	 * @param element - add one element to the lineStorage
	 */
	private void addElement(IDatabaseElement<Key, Value> element) {
		try {
			lineStorageKeys.appendLine(keyFactory.createString(CompletableFuture.completedFuture(element.getKey())).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException();
		}
		
		try {
			lineStorageValues.appendLine(valueFactory.createString(CompletableFuture.completedFuture(element.getValue())).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * @param - list of elements to add at once.
	 */
	@Override
	public void add(List<? extends IDatabaseElement<Key, Value>> elements) {
		elements.stream()
		.sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
				.forEach(e -> addElement(e));
	}
	
	private CompletableFuture<Integer> findElementByIDRec(Key keyToFind, CompletableFuture<Integer> lowLine,
														CompletableFuture<Integer> highLine) {
		CompletableFuture<Integer> currentLine = lowLine.thenCombine(highLine, (low, high) -> {
			return (low + high) / 2;
		});				
			
		CompletableFuture<Key> currentkey = currentLine.thenCompose((i) -> { return this.getKeyByIndex(i);});
//		return currentkey.thenCompose((k) -> {
//			
//			int compareResult = k.compareTo(keyToFind);
//			if (compareResult == 0) {
//				return currentLine;
//			} else if (compareResult < 0) {
//				CompletableFuture<Integer> newLowLine = currentLine.thenApply((x) -> x + 1);
//				
//				return findElementByIDRec(keyToFind, newLowLine, highLine);
//			} else {
//				CompletableFuture<Integer> newHighLine = currentLine.thenApply((x) -> x - 1);
//				
//				return findElementByIDRec(keyToFind, lowLine, newHighLine);
//			} 
//		});
		
		CompletableFuture<Integer> searchStatus = lowLine.thenCombine(highLine, (low, high) -> {
			if (low > high) {
				return Database.SEARCH_OUT_OF_BOUND;
			} else if (low == high) {
				return Database.SEARCH_LAST_INTERATION;
			} else {
				return Database.SEARCH_CAN_CONTINUE;
			}
		});
				
		CompletableFuture<Integer> result = currentkey.thenCombine(searchStatus, (key, status) -> {
			if ((status == SEARCH_OUT_OF_BOUND) || (status == SEARCH_LAST_INTERATION && key.compareTo(keyToFind) != 0)) {
				/* no need to continue search - we are out of bounds or in the last iteration and keys aren't equals */
				return -1;
			} else {
				/* the comparison can be -1 this way */
				return key.compareTo(keyToFind) * 2;
			}
		});
		
		return result.thenCompose(new Function<Integer, CompletionStage<Integer>>() {

			@Override
			public CompletionStage<Integer> apply(Integer compareResult) {
				
				if (compareResult == -1) {
					return CompletableFuture.completedFuture(-1);
				}
				
				if (compareResult == 0) {
					return currentLine;
				} else if (compareResult < 0) {
					CompletableFuture<Integer> newLowLine = currentLine.thenApply((x) -> x + 1);
					
					return findElementByIDRec(keyToFind, newLowLine, highLine);
				} else {
					CompletableFuture<Integer> newHighLine = currentLine.thenApply((x) -> x - 1);
					
					return findElementByIDRec(keyToFind, lowLine, newHighLine);
				} 
			}
		});
		
//		return currentkey.thenCompose(new Function<Key, CompletionStage<Integer>>() {
//
//			@Override
//			public CompletionStage<Integer> apply(Key k) {
//				int compareResult = k.compareTo(keyToFind);
//				boolean notFound;
//				
//				lowLine.thenCombine(highLine, (low, high) -> {
//					if (low > high) {
//						BOOM!;
//						notFound = true;
//					} else {
//						notFound = false;
//					}
//					
//					return 0;
//				});		
//				
//				if (notFound) {
//					return CompletableFuture.completedFuture(-1);
//				}
//				
//				if (compareResult == 0) {
//					return currentLine;
//				} else if (compareResult < 0) {
//					CompletableFuture<Integer> newLowLine = currentLine.thenApply((x) -> x + 1);
//					
//					return findElementByIDRec(keyToFind, newLowLine, highLine);
//				} else {
//					CompletableFuture<Integer> newHighLine = currentLine.thenApply((x) -> x - 1);
//					
//					return findElementByIDRec(keyToFind, lowLine, newHighLine);
//				} 
//			}
//		});
	}
	
	/**
	 * @param - key of the wanted element
	 * @return - if key exists,  Optional<Value> of the element. else, Optionl.empty().
	 */
	@Override
	public CompletableFuture<Optional<Value>> findElementByID(Key key) {
		CompletableFuture<Integer> index = findElementByIDRec(key, CompletableFuture.completedFuture(0),
				getNumberOfElements().thenApply(x -> x - 1));
		
		return index.thenCompose((i) -> {
			if (i == -1) {
				return CompletableFuture.completedFuture(Optional.empty());
			} else {
				return getValueByIndex(i).thenApply((v) -> Optional.of(v));
			}
		});
	}
}
