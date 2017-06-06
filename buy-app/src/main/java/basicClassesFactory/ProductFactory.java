package basicClassesFactory;

import java.util.concurrent.CompletableFuture;

import basicClasses.Product;
import databaseInterfaces.IStringableFactory;

public class ProductFactory implements IStringableFactory<Product> {

	private final static String PRODUCT_SPLITER = ",";

	@Override
	public CompletableFuture<Product> createObject(CompletableFuture<String> s) {
		return s.thenApply(strElement -> {
			String[] elementString = strElement.split(PRODUCT_SPLITER);
			
			return new Product(elementString[0], Long.valueOf(elementString[1]));
		});
	}

	@Override
	public CompletableFuture<String> createString(CompletableFuture<Product> e) {
		return e.thenApply(element -> {
			return element.getId() + PRODUCT_SPLITER + element.getPrice().toString();
		});
	}
}
