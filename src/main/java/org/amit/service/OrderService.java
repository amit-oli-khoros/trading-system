package org.amit.service;

import org.amit.common.OrderIdGenerator;
import org.amit.models.CreateOrderRequest;
import org.amit.models.Order;
import org.amit.models.OrderStatus;
import org.amit.models.UpdateOrderRequest;
import org.amit.store.OrderStore;

import java.time.Instant;
import java.util.List;

public class OrderService {
    private final OrderStore orderStore;

    public OrderService(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    public Order createOrder(CreateOrderRequest createOrderRequest) {
        Order order = Order.builder()
                .id(OrderIdGenerator.nextId())
                .userId(createOrderRequest.getUserId())
                .stockSymbol(createOrderRequest.getStockSymbol())
                .orderType(createOrderRequest.getOrderType())
                .quantity(createOrderRequest.getQuantity())
                .price(createOrderRequest.getPrice())
                .orderPlacedAt(Instant.now())
                .orderStatus(OrderStatus.ACCEPTED)
                .build();
        return orderStore.createOrder(order);
    }

    public Order updateOrder(Long orderId, UpdateOrderRequest updateOrderRequest){
        return orderStore.updateOrder(orderId, updateOrderRequest);
    }

    public Order cancelOrder(Long orderId){
        return orderStore.cancelOrder(orderId);
    }

    public OrderStatus queryOrderStatus(Long orderId){
        return orderStore.queryOrderStatus(orderId);
    }

    public List<Order> getPendingOrders(String stockSymbol){
        return orderStore.getPendingOrders(stockSymbol);
    }
}
