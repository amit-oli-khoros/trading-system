package org.amit.store;

import org.amit.models.Order;
import org.amit.models.OrderStatus;
import org.amit.models.OrderType;
import org.amit.models.UpdateOrderRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryOrderStore implements OrderStore{
    private Map<String, PriorityQueue<Order>> buyOrderBook = new ConcurrentHashMap<>();
    private Map<String, PriorityQueue<Order>> sellOrderBook = new ConcurrentHashMap<>();
    private Map<String, ReentrantLock> stockLockMap = new ConcurrentHashMap<>();
    private Map<Long, Order> ordersById = new ConcurrentHashMap<>();

    public ReentrantLock getLock(String stockSymbol) {
        return stockLockMap.computeIfAbsent(stockSymbol, s->new ReentrantLock());
    }

    public Map<String, PriorityQueue<Order>> getBuyOrderBook() {
        return buyOrderBook;
    }

    public Map<String, PriorityQueue<Order>> getSellOrderBook() {
        return sellOrderBook;
    }

    @Override
    public List<String> getAllStockSymbols() throws IOException {
        Path filePath = Paths.get("src/main/resources/stock-symbols.txt");
        return Files.readAllLines(filePath);//works for demonstration right now
    }

    @Override
    public Order createOrder(Order order) {
        ReentrantLock lock = getLock(order.getStockSymbol());
        lock.lock();
        try{
            if(order.getOrderType() == OrderType.BUY) {
                buyOrderBook.computeIfAbsent(
                        order.getStockSymbol(), symbol-> new PriorityQueue<>(Order.BUY_ORDER_COMPARATOR)
                ).add(order);
            }
            else if (order.getOrderType() == OrderType.SELL) {
                sellOrderBook.computeIfAbsent(
                        order.getStockSymbol(), symbol-> new PriorityQueue<>(Order.SELL_ORDER_COMPARATOR)
                ).add(order);
            }
            ordersById.put(order.getId(), order);
            return order;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Order updateOrder(Long orderId, UpdateOrderRequest updateOrderRequest) {
        Order existingOrder = ordersById.get(orderId);
        if(existingOrder == null || existingOrder.getOrderStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order not found or already processed");
        }
        ReentrantLock lock = getLock(existingOrder.getStockSymbol());
        lock.lock();
        try{
            getOrderBook(existingOrder).remove(existingOrder);
            Order updatedOrder = existingOrder
                    .withUpdatedQuantity(updateOrderRequest.getQuantity())
                    .withUpdatedPrice(updateOrderRequest.getPrice());

            getOrderBook(existingOrder).add(updatedOrder);
            ordersById.put(orderId, updatedOrder);
            return updatedOrder;
        }
        finally {
            lock.unlock();
        }
    }

    private PriorityQueue<Order> getOrderBook(Order order) {
        return order.getOrderType() == OrderType.BUY ? buyOrderBook.get(order.getStockSymbol()) : sellOrderBook.get(order.getStockSymbol());
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order existingOrder = ordersById.get(orderId);
        if(existingOrder == null || existingOrder.getOrderStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order not found or already processed");
        }
        ReentrantLock lock = getLock(existingOrder.getStockSymbol());
        lock.lock();
        try{
            getOrderBook(existingOrder).remove(existingOrder);
            Order cancelledOrder = existingOrder.withUpdatedStatus(OrderStatus.CANCELED);
            ordersById.put(orderId, cancelledOrder);
            return cancelledOrder;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderStatus queryOrderStatus(Long orderId) {
        Order order = ordersById.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return order.getOrderStatus();
    }

    @Override
    public List<Order> getPendingOrders(String stockSymbol) {
        ReentrantLock lock = getLock(stockSymbol);
        lock.lock();
        try {
            List<Order> result = new ArrayList<>();
            if (buyOrderBook.containsKey(stockSymbol)) {
                result.addAll(buyOrderBook.get(stockSymbol));
            }
            if (sellOrderBook.containsKey(stockSymbol)) {
                result.addAll(sellOrderBook.get(stockSymbol));
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}
