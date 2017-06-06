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
		
	/**
	 * 
	 * @param parsedOrder - all orders which have the same order id that were parsed from the user
	 * @param products - list of all products which were given by the user
	 * @return - DatabaseElement of all relevant orders with the same order id
	 */
	private Optional<DatabaseElement> fixOrderOperations(DatabaseElement parsedOrder, List<Product> products) {
		DatabaseElement fixedOrder;
		List<Order> relevantOrderOperations = new ArrayList<>();
		List<String> productIDs = products.stream().map(p -> p.getId()).collect(Collectors.toList());
		
		/* find last commit order operation */
		Collections.reverse(parsedOrder.getOrdersList());
		
		for (Order orderOperation : parsedOrder.getOrdersList()) {
			relevantOrderOperations.add(orderOperation);
			
			if (orderOperation.getType().equals(Order.COMMIT_ORDER_TYPE)) {
				/* all operations before last COMMIT_ORDER_TYPE are not relevant to store */
				break;
			}
		}
		
		/* return order to normal order */
		Collections.reverse(relevantOrderOperations);
		
		fixedOrder = new DatabaseElement(parsedOrder.getId(), relevantOrderOperations);
		
		/* product ID must be valid */
		if (productIDs.contains(fixedOrder.getOrdersList().get(0).getProductID())) {
			return Optional.of(fixedOrder);
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * 
	 * @param products - list of all products which were given by the user
	 * @param allParsedOrders - List of all Database elements. each Database element contains list of orders with the same
	 * 							order ID.
	 * @return - List of only the relevant orders
	 */
	private List<DatabaseElement> removeIrrelevantOrders(List<Product> products,
													List<DatabaseElement> allParsedOrders) {
		List<DatabaseElement> relevantOrders = new ArrayList<>();
		
		for (DatabaseElement order : allParsedOrders) {
			Optional<DatabaseElement> relevantOrder = fixOrderOperations(order, products);
			
			if (relevantOrder.isPresent()) {
				relevantOrders.add(relevantOrder.get());
			}
		}
		
		return relevantOrders;
	}
	
	/**
	 * This function initialize the databases.
	 * 
	 * @param products - list of all products which were given by the user
	 * @param allParsedOrders - List of all Database elements. each Database element contains list of orders with the same
	 * 							order ID.
	 * @return - Void
	 */
	private CompletableFuture<Void> initializeDatabase(List<Product> products, List<DatabaseElement> allParsedOrders)
	{
		List<DatabaseElement> ordersAfterFixup;
		List<DatabaseElement> ordersByUsers = new ArrayList<DatabaseElement>();
		List<DatabaseElement> ordersByProducts = new ArrayList<DatabaseElement>();
		Map<String, DatabaseElement> ordersByUsersIDMap = new HashMap<String, DatabaseElement>();
		Map<String, DatabaseElement> ordersByProductsIDMap = new HashMap<String, DatabaseElement>();
		
		ordersAfterFixup = removeIrrelevantOrders(products, allParsedOrders);

		ordersByOrdersIDDatabase.add(ordersAfterFixup);	
		
		/* we need deep clone to work on lists */
		for (DatabaseElement element : ordersAfterFixup) {
			ordersByUsers.add(new DatabaseElement(element));
			ordersByProducts.add(new DatabaseElement(element));
		}
		
		for (DatabaseElement element : ordersByUsers) {
			String userID = element.getOrdersList().get(0).getClientID();
			
			if (ordersByUsersIDMap.containsKey(userID)) {
				List<Order> orders = ordersByUsersIDMap.get(userID).getOrdersList();
				
				orders.addAll(element.getOrdersList());
				ordersByUsersIDMap.put(userID, new DatabaseElement(userID, orders));
			} else {
				ordersByUsersIDMap.put(userID, new DatabaseElement(userID, element.getOrdersList()));
			}
		}
		ordersByUsersIDDatabase.add(new ArrayList<DatabaseElement>(ordersByUsersIDMap.values()));
		
		for (DatabaseElement element : ordersByProducts) {
			String productID = element.getOrdersList().get(0).getProductID();
			
			if (ordersByProductsIDMap.containsKey(productID)) {
				List<Order> orders = ordersByProductsIDMap.get(productID).getOrdersList();
				
				orders.addAll(element.getOrdersList());
				ordersByProductsIDMap.put(productID, new DatabaseElement(productID, orders));
			} else {
				ordersByProductsIDMap.put(productID, new DatabaseElement(productID, element.getOrdersList()));
			}
		}
		ordersByProductsIDDatabase.add(new ArrayList<DatabaseElement>(ordersByProductsIDMap.values()));
		
		productsDatabase.add(products);
				
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> setupXml(String xmlData) {
		try {
			return initializeDatabase(ProductsParserXml.createListOfProducts(xmlData), ProductsParserXml.createListOfOrders(xmlData));
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	@Override
	public CompletableFuture<Void> setupJson(String jsonData) {
		return initializeDatabase(ProductsParserJson.createListOfProducts(jsonData), ProductsParserJson.createListOfOrders(jsonData));
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
				
				while (operationIndex >= 0) {
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
		return ordersByOrdersIDDatabase.findElementByID(orderId).thenApply(order -> {
			List<Integer> productAmounts = new ArrayList<Integer>();
			
			if (order.isPresent()) {
				List<Order> operations = order.get().getOrdersList();
				int currentIndex = 0;

				for (Order operation : operations) {
					if (!operation.getType().equals(Order.CANCEL_ORDER_TYPE)) {
						/* adding only products amounts which weren't canceled */
						productAmounts.add(Integer.valueOf(operation.getNumOfProducts()));
					} else if ((operation.getType().equals(Order.CANCEL_ORDER_TYPE)) && (currentIndex != operations.size() - 1)) {
						productAmounts.add(-1);
					}
				}
			}
			
			return productAmounts;
		});
	}

	@Override
	public CompletableFuture<List<String>> getOrderIdsForUser(String userId) {
		return ordersByUsersIDDatabase.findElementByID(userId).thenApply(order -> {
			List<String> userIDs = new ArrayList<>();
			
			if (order.isPresent()) {
				List<Order> operations = 
						order.get()
						.getOrdersList();
				
				userIDs = operations.stream()
						.map(operation -> operation.getOrderID())
						.distinct()
						.sorted()
						.collect(Collectors.toList());
			}
			
			return userIDs;
		});
	}

	@Override
	public CompletableFuture<Long> getTotalAmountSpentByUser(String userId) {
		return ordersByUsersIDDatabase.findElementByID(userId).thenApply(order -> {
			Long totalAmount = (long) 0;
			
			if (order.isPresent()) {
				Map<String, List<Order>> operationsPerOrderID = 
						order.get()
						.getOrdersList()
						.stream()
						.collect(Collectors.groupingBy(Order::getOrderID));
				
				for (List<Order> operations : operationsPerOrderID.values()) {
					if (!operations.get(operations.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE)) {
						/* calculation per order id: getting the amount of the last order operation multiply the product price */
						try {
							totalAmount += Integer.valueOf(operations.get(operations.size() - 1).getNumOfProducts()) * 
									productsDatabase.findElementByID(operations.get(0).getProductID()).get().get().getPrice();
						} catch (InterruptedException | ExecutionException e) {
							throw new RuntimeException();
						}
					}
				}
			}
			
			return totalAmount;
		});
	}

	@Override
	public CompletableFuture<List<String>> getUsersThatPurchased(String productId) {
		return ordersByProductsIDDatabase.findElementByID(productId).thenApply(order -> {
			List<String> userIDs = new ArrayList<>();
			
			if (order.isPresent()) {
				Map<String, List<Order>> operationsPerOrderID = 
						order.get()
						.getOrdersList()
						.stream()
						.collect(Collectors.groupingBy(Order::getOrderID));
				
				for (List<Order> orders : operationsPerOrderID.values()) {
					if ((!orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE)) &&
					    (!userIDs.contains(orders.get(0).getClientID()))) {
						userIDs.add(orders.get(0).getClientID());
					}
				}
			}
			
			return userIDs.stream().sorted().collect(Collectors.toList());
		});
	}

	@Override
	public CompletableFuture<List<String>> getOrderIdsThatPurchased(String productId) {
		return ordersByProductsIDDatabase.findElementByID(productId).thenApply(order -> {
			List<String> orderIDs = new ArrayList<>();
			
			if (order.isPresent()) {
				orderIDs = order.get()
						  .getOrdersList()
						  .stream()
						  .map(operation -> operation.getOrderID())
						  .distinct()
						  .sorted()
						  .collect(Collectors.toList());
			}
			
			return orderIDs;
		});
	}

	@Override
	public CompletableFuture<OptionalLong> getTotalNumberOfItemsPurchased(String productId) {
		return ordersByProductsIDDatabase.findElementByID(productId).thenApply(order -> {			
			if (order.isPresent()) {
				Long sum = (long) 0;
				Map<String, List<Order>> operationsPerOrderID = 
						order.get()
						.getOrdersList()
						.stream()
						.collect(Collectors.groupingBy(Order::getOrderID));
				
				for (List<Order> orders : operationsPerOrderID.values()) {
					if (!orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE)) {
						sum += orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE) ?
								0 : orders.get(orders.size() - 1).getNumOfProducts();
					}
				}
				
				return OptionalLong.of(sum);
			}
			
			return OptionalLong.empty();
		});
	}

	@Override
	public CompletableFuture<OptionalDouble> getAverageNumberOfItemsPurchased(String productId) {
		return ordersByProductsIDDatabase.findElementByID(productId).thenApply(order -> {			
			if (order.isPresent()) {
				Double sum = (double) 0;
				Integer amount = 0;
				
				Map<String, List<Order>> operationsPerOrderID = 
						order.get()
						.getOrdersList()
						.stream()
						.collect(Collectors.groupingBy(Order::getOrderID));
				
				for (List<Order> orders : operationsPerOrderID.values()) {
					if (!orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE)) {
						sum += orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE) ?
								0 : orders.get(orders.size() - 1).getNumOfProducts();
						
						++amount;
					}
				}
				
				return OptionalDouble.of(amount != 0 ? sum / amount : 0);
			}
			
			return OptionalDouble.empty();
		});
	}

	private CompletableFuture<OptionalDouble> getRatioForUser(String userId, String orderType) {
		return ordersByUsersIDDatabase.findElementByID(userId).thenApply(order -> {			
			if (order.isPresent()) {
				Integer counter = 0;
				
				Map<String, List<Order>> operationsPerOrderID = 
						order.get()
						.getOrdersList()
						.stream()
						.collect(Collectors.groupingBy(Order::getOrderID));
				
				for (List<Order> orders : operationsPerOrderID.values()) {
					if ((orderType.equals(Order.MODIFY_ORDER_TYPE)) && (orders.size() > 1)) {
						/* Modified ratio */
						++counter;
					} else if (orders.get(orders.size() - 1).getType().equals(orderType)) {
						/* Canceled ratio */
						++counter;
					}
				}
				
				return OptionalDouble.of((operationsPerOrderID.size() == 0) ? 0 : counter / (double) operationsPerOrderID.size());
			}
			
			return OptionalDouble.empty();
		});
	}
	
	@Override
	public CompletableFuture<OptionalDouble> getCancelRatioForUser(String userId) {
		return getRatioForUser(userId, Order.CANCEL_ORDER_TYPE);
	}

	@Override
	public CompletableFuture<OptionalDouble> getModifyRatioForUser(String userId) {
		return getRatioForUser(userId, Order.MODIFY_ORDER_TYPE);
	}

	@Override
	public CompletableFuture<Map<String, Long>> getAllItemsPurchased(String userId) {
		return ordersByUsersIDDatabase.findElementByID(userId).thenApply(order -> {	
			Map<String, Long> productCounters = new HashMap<>();
			
			if (order.isPresent()) {			
				Map<String, List<Order>> operationsPerOrderID = 
						order.get()
						.getOrdersList()
						.stream()
						.collect(Collectors.groupingBy(Order::getOrderID));
				
				for (List<Order> orders : operationsPerOrderID.values()) {
					if (!orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE)) {
						String productID = orders.get(0).getProductID();
						Long numOfProduct = orders.get(orders.size() - 1).getNumOfProducts().longValue();
						
						if (productCounters.containsKey(productID)) {
							productCounters.put(productID, productCounters.get(productID) + numOfProduct);
						} else {
							productCounters.put(productID, numOfProduct);
						}
					}
				}
			}
			
			return productCounters;
		});
	}

	@Override
	public CompletableFuture<Map<String, Long>> getItemsPurchasedByUsers(String productId) {
		return ordersByProductsIDDatabase.findElementByID(productId).thenApply(order -> {	
			Map<String, Long> userCounters = new HashMap<>();
			
			if (order.isPresent()) {			
				Map<String, List<Order>> operationsPerOrderID = 
						order.get()
						.getOrdersList()
						.stream()
						.collect(Collectors.groupingBy(Order::getOrderID));
				
				for (List<Order> orders : operationsPerOrderID.values()) {
					if (!orders.get(orders.size() - 1).getType().equals(Order.CANCEL_ORDER_TYPE)) {
						String userID = orders.get(0).getClientID();
						Long numOfProduct = orders.get(orders.size() - 1).getNumOfProducts().longValue();
						
						if (userCounters.containsKey(userID)) {
							userCounters.put(userID, userCounters.get(userID) + numOfProduct);
						} else {
							userCounters.put(userID, numOfProduct);
						}
					}
				}
			}
			
			return userCounters;
		});
	}
}
