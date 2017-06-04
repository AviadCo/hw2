package basicClassesFactory;

import java.util.concurrent.CompletableFuture;

import databaseInterfaces.IStringableFactory;

/**
 * This class is a factory for String elements.
 * The class implements IStringableFactory factory
 * 
 * @author Aviad
 *
 */
public class StringFactory implements IStringableFactory<String> {

	@Override
	public CompletableFuture<String> createObject(CompletableFuture<String> s) {
		return s.thenApply(str -> str);
	}

	@Override
	public CompletableFuture<String> createString(CompletableFuture<String> e) {
		return e.thenApply(str -> str);
	}

}
