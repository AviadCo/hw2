package databaseImplementations;

import java.util.concurrent.CompletableFuture;

import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorageFactory;

/**
 * This class implements LineStorageFactory to create MapBasedStorage objects.
 * 
 * @author Aviad
 *
 */
public class MapBasedStorageFactory implements FutureLineStorageFactory {

	@Override
	public CompletableFuture<FutureLineStorage> open(String arg0) throws IndexOutOfBoundsException {
		return CompletableFuture.completedFuture(new MapBasedStorage());
	}

}
