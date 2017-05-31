package productsManagerImplementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import basicClasses.DatabaseElement;
import basicClasses.Order;
import basicClasses.Product;
import databaseImplementations.Database;
import databaseInterfaces.IDatabaseElement;
import il.ac.technion.cs.sd.buy.app.BuyProductInitializer;
import il.ac.technion.cs.sd.buy.app.BuyProductReader;

/**
 * This class implements BuyProductInitializer & BuyProductReader using FutureLineStorageFactory
 * 
 * @author Aviad
 *
 */
public class ProductManager implements BuyProductInitializer, BuyProductReader {
	
	/* Databases names */
	public static final String ORDERS_BY_ORDERS_ID_DATA_BASE_NAME   = "ORDERS_BY_ORDERS_ID_DATABASE";
	public static final String ORDERS_BY_USERS_ID_DATA_BASE_NAME    = "ORDERS_BY_USERS_ID_DATABASE";
	public static final String ORDERS_BY_PRODUCTS_ID_DATA_BASE_NAME = "ORDERS_BY_PRODUCTS_ID_DATABASE";
	public static final String PRODUCTS_DATA_BASE_NAME              = "PRODUCTS_DATABASE";
	
	Database<String, DatabaseElement> ordersByOrdersIDDatabase;
	Database<String, DatabaseElement> ordersByUsersIDDatabase;
	Database<String, DatabaseElement> ordersByProductsIDDatabase;
	Database<String, Product> productsDatabase;
	
	@Inject
	public ProductManager(@Named(ORDERS_BY_ORDERS_ID_DATA_BASE_NAME) Database<String, DatabaseElement> ordersByOrdersIDDatabase,
						  @Named(ORDERS_BY_USERS_ID_DATA_BASE_NAME) Database<String, DatabaseElement> ordersByUsersIDDatabase,
						  @Named(ORDERS_BY_USERS_ID_DATA_BASE_NAME) Database<String, DatabaseElement> ordersByProductsIDDatabase,
						  @Named(PRODUCTS_DATA_BASE_NAME) Database<String, Product> productsDatabase) {
		this.ordersByOrdersIDDatabase   = ordersByOrdersIDDatabase;
		this.ordersByUsersIDDatabase    = ordersByUsersIDDatabase;
		this.ordersByProductsIDDatabase = ordersByProductsIDDatabase;
		this.productsDatabase  			= productsDatabase;
	}
		
	private Optional<DatabaseElement> fixOrderOperations(DatabaseElement parsedOrder, List<Product> products) {
		DatabaseElement fixedOrder;
		List<Order> relevantOrderOperations = new ArrayList<>();
		List<String> productIDs = products.stream().map(p -> p.getId()).collect(Collectors.toList());
		
		/* find last commit order operation */
		Collections.reverse(parsedOrder.getOrdersList());
		
		for (Order orderOperation : parsedOrder.getOrdersList()) {
			relevantOrderOperations.add(orderOperation);
			
			if (orderOperation.equals(Order.COMMIT_ORDER_TYPE)) {
				/* all operations before last COMMIT_ORDER_TYPE are not relevant to store */
				break;
			}
		}
		
		/* return order to normal order */
		Collections.reverse(relevantOrderOperations);
		
		fixedOrder = new DatabaseElement(parsedOrder.getId(), relevantOrderOperations);
		
		if (productIDs.contains(fixedOrder.getOrdersList().get(0).getProductID())) {
			return Optional.of(fixedOrder);
		} else {
			return Optional.empty();
		}
	}
	
	private CompletableFuture<List<DatabaseElement>> removeIrrelevantOrders(CompletableFuture<List<Product>> products,
													CompletableFuture<List<DatabaseElement>> allParsedOrders) {
		return allParsedOrders.thenCombine(products, (orders, productsList) -> {
			List<DatabaseElement> relevantOrders = new ArrayList<>();
			
			for (DatabaseElement order : orders) {
				Optional<DatabaseElement> relevantOrder = fixOrderOperations(order, productsList);
				
				if (relevantOrder.isPresent()) {
					relevantOrders.add(relevantOrder.get());
				}
			}
			
			return relevantOrders;
		});
	}
	
	@Override
	public CompletableFuture<Void> setupXml(String xmlData) {
		CompletableFuture<List<Product>> products;
		CompletableFuture<List<DatabaseElement>> allParsedOrders;
		CompletableFuture<List<DatabaseElement>> ordersAfterFixup;
		
		try {
			products = CompletableFuture.completedFuture(ProductsParserXml.createListOfProducts(xmlData));
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new RuntimeException();
		}
		
		try {
			allParsedOrders = CompletableFuture.completedFuture(ProductsParserXml.createListOfOrders(xmlData));
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new RuntimeException();
		}
		
		ordersAfterFixup = removeIrrelevantOrders(products, allParsedOrders);

		//TODO fix this		
//		try {
//			ordersByOrdersIDDatabase.add(ordersAfterFixup);	
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException();
//		}
//		
//		try {
//			ordersByUsersIDDatabase.add(ordersAfterFixup.thenApply(orders -> {
//				for(DatabaseElement order : orders) {
//					order.setId(order.getOrdersList().get(0).getClientID());
//				}
//				
//				return orders;
//			}));
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException();
//		}
//		
//		try {
//			ordersByProductsIDDatabase.add(ordersAfterFixup.thenApply(orders -> {
//				for(DatabaseElement order : orders) {
//					order.setId(order.getOrdersList().get(0).getProductID());
//				}
//				
//				return orders;
//			}));
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException();
//		}
		
//		productsDatabase.add(products);
				
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> setupJson(String jsonData) {
		return null;
	}

	@Override
	public CompletableFuture<Boolean> isValidOrderId(String orderId) {
		return ordersByOrdersIDDatabase.findElementByID(orderId).thenApply(order -> {
			return order.isPresent();
		});
	}

	@Override
	public CompletableFuture<Boolean> isCanceledOrder(String orderId) {
		return ordersByOrdersIDDatabase.findElementByID(orderId).thenApply(order -> {
			if (order.isPresent()) {
				List<Order> orders = order.get().getOrdersList();
				
				return orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE);
			} else {
				return false;
			}
		});
	}

	@Override
	public CompletableFuture<Boolean> isModifiedOrder(String orderId) {
		return ordersByOrdersIDDatabase.findElementByID(orderId).thenApply(order -> {
			if (order.isPresent()) {
				//TODO check what is modified...
				List<Order> orders = order.get().getOrdersList();
				
				for (Order orderOperation : orders) {
					if (orderOperation.getType().equals(Order.MODIFY_ORDER_TYPE)) {
						return true;
					}
				}
				
				return false;
			} else {
				return false;
			}
		});
	}

	@Override
	public CompletableFuture<OptionalInt> getNumberOfProductOrdered(String orderId) {
		return ordersByOrdersIDDatabase.findElementByID(orderId).thenApply(order -> {
			if (order.isPresent()) {
				List<Order> orders = order.get().getOrdersList();
				int operationIndex = orders.size() - 1;
				Integer numOfProducts = 0;
				
				while (operationIndex > 0) {
					Order operation = orders.get(operationIndex);
					
					if (!operation.getType().equals(Order.CANCEL_ORDER_TYPE)) {
						numOfProducts = operation.getNumOfProducts();
						break;
					}
					
					--operationIndex;
				}
								
				if (orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE)) {
					return OptionalInt.of(numOfProducts * -1);	
				} else {
					return OptionalInt.of(numOfProducts); 
				}
			} else {
				return OptionalInt.empty();
			}
		});
	}

	@Override
	public CompletableFuture<List<Integer>> getHistoryOfOrder(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<String>> getOrderIdsForUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Long> getTotalAmountSpentByUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<String>> getUsersThatPurchased(String productId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<String>> getOrderIdsThatPurchased(String productId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<OptionalLong> getTotalNumberOfItemsPurchased(String productId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<OptionalDouble> getAverageNumberOfItemsPurchased(String productId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<OptionalDouble> getCancelRatioForUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<OptionalDouble> getModifyRatioForUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Map<String, Long>> getAllItemsPurchased(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Map<String, Long>> getItemsPurchasedByUsers(String productId) {
		// TODO Auto-generated method stub
		return null;
	}
}
