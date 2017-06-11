package il.ac.technion.cs.sd.buy.test;

import java.util.concurrent.ExecutionException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import basicClasses.DatabaseElement;
import basicClasses.Product;
import basicClassesFactory.DatabaseElementFactory;
import basicClassesFactory.ProductFactory;
import basicClassesFactory.StringFactory;
import databaseImplementations.Database;
import databaseImplementations.MapBasedStorageFactory;
import il.ac.technion.cs.sd.buy.app.BuyProductInitializer;
import il.ac.technion.cs.sd.buy.app.BuyProductReader;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorageFactory;
import productsManagerImplementations.ProductManager;

// This module is in the testing project, so that it could easily bind all dependencies from all levels.
public class BuyProductModule extends AbstractModule {
  
	private static final String KEYS = "KEYS";
	private static final String VALUES = "VALUES";

	@Override
	protected void configure() {
		  bind(BuyProductInitializer.class).to(ProductManager.class);
		  bind(BuyProductReader.class).to(ProductManager.class);
	}
	
	@Provides
	@Singleton 
	@Named(ProductManager.ORDERS_BY_ORDERS_ID_DATA_BASE_NAME)
	Database<String, DatabaseElement> createOrdersByOrderIDDatabases(FutureLineStorageFactory futureLineStorageFactory) {
		  try {
			return new Database<String, DatabaseElement>(futureLineStorageFactory.open(ProductManager.ORDERS_BY_ORDERS_ID_DATA_BASE_NAME + KEYS).get(),
					  									   futureLineStorageFactory.open(ProductManager.ORDERS_BY_ORDERS_ID_DATA_BASE_NAME + VALUES).get(),
					  									   new StringFactory(), new DatabaseElementFactory());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException();
		}
	}
	
	@Provides
	@Singleton 
	@Named(ProductManager.ORDERS_BY_USERS_ID_DATA_BASE_NAME)
	Database<String, DatabaseElement> createOrdersByUserIDDatabases(FutureLineStorageFactory futureLineStorageFactory) {
		  try {
			return new Database<String, DatabaseElement>(futureLineStorageFactory.open(ProductManager.ORDERS_BY_USERS_ID_DATA_BASE_NAME + KEYS).get(),
					  									   futureLineStorageFactory.open(ProductManager.ORDERS_BY_USERS_ID_DATA_BASE_NAME + VALUES).get(),
					  									   new StringFactory(), new DatabaseElementFactory());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException();
		}
	}
	
	@Provides
	@Singleton 
	@Named(ProductManager.ORDERS_BY_PRODUCTS_ID_DATA_BASE_NAME)
	Database<String, DatabaseElement> createOrdersByProductIDDatabases(FutureLineStorageFactory futureLineStorageFactory) {
		  try {
			return new Database<String, DatabaseElement>(futureLineStorageFactory.open(ProductManager.ORDERS_BY_PRODUCTS_ID_DATA_BASE_NAME + KEYS).get(),
					  									   futureLineStorageFactory.open(ProductManager.ORDERS_BY_PRODUCTS_ID_DATA_BASE_NAME + VALUES).get(),
					  									   new StringFactory(), new DatabaseElementFactory());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException();
		}
	}
	
	@Provides
	@Singleton 
	@Named(ProductManager.PRODUCTS_DATA_BASE_NAME)
	Database<String, Product> createProductsDatabases(FutureLineStorageFactory futureLineStorageFactory) {
		  try {
			return new Database<String, Product>(futureLineStorageFactory.open(ProductManager.PRODUCTS_DATA_BASE_NAME + KEYS).get(),
					  							 futureLineStorageFactory.open(ProductManager.PRODUCTS_DATA_BASE_NAME + VALUES).get(),
					  							 new StringFactory(), new ProductFactory());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException();
		}
	}
}
