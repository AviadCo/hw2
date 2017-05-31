package databaseImplementations;

import java.util.concurrent.CompletableFuture;

import databaseInterfaces.IStringableFactory;

/**
 * This class is a factory for String elements.
 * The class implements IStringableFactory factory
 * 
 * @author Aviad
 *
 */
public class StringFactoryTester implements IStringableFactory<String> {

	@Override
	public CompletableFuture<String> createObject(CompletableFuture<String> s) {
		return s;
	}

	@Override
	public CompletableFuture<String> createString(CompletableFuture<String> e) {
		return e;
	}

}
