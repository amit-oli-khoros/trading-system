package org.amit.store;

import org.amit.models.Order;
import org.amit.models.OrderStatus;
import org.amit.models.UpdateOrderRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public interface OrderStore {
    List<String> getAllStockSymbols() throws IOException;
    ReentrantLock getLock(String stockSymbol);
    Map<String, PriorityQueue<Order>> getBuyOrderBook() ;
    Map<String, PriorityQueue<Order>> getSellOrderBook();
    Order createOrder(Order order);
    Order updateOrder(Long orderId, UpdateOrderRequest updateOrderRequest);
    Order cancelOrder(Long orderId);
    OrderStatus queryOrderStatus(Long orderId);
    List<Order> getPendingOrders(String stockSymbol);
}
