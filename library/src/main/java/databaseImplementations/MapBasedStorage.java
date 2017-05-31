package databaseImplementations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;

/**
 * This class implements LineStorage to store objects.
 * The MapBasedStorage isn't persistent (just for testing).
 * 
 * @author Aviad
 *
 */
public class MapBasedStorage implements FutureLineStorage {

	private Map<Integer, String> mapStorage;
	
	public MapBasedStorage() {
		this.mapStorage = new HashMap<Integer, String>();
	}
	
	@Override
	public CompletableFuture<Void> appendLine(String item) {
		mapStorage.put(mapStorage.size(), item);
		
		return null;
	}

	@Override
	public CompletableFuture<Integer> numberOfLines() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
		
		return CompletableFuture.completedFuture(mapStorage.size());
	}

	@Override
	public CompletableFuture<String> read(int index) {
		if (mapStorage.containsKey(index)) {
			String result = mapStorage.get(index);
			
			try {
				Thread.sleep(result.length());
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
			
			return CompletableFuture.completedFuture(result);
		} else {
			return null;
		}
	}

}
