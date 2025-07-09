package org.amit.processors;

import org.amit.models.CreateOrderRequest;
import org.amit.models.Order;
import org.amit.models.OrderStatus;
import org.amit.models.UpdateOrderRequest;
import org.amit.service.OrderService;

import java.util.List;

public class OrderProcessor {
    OrderService orderService;

    public OrderProcessor(OrderService orderService) {
        this.orderService = orderService;
    }

    public Order placeOrder(CreateOrderRequest createOrderRequest) {
        return orderService.createOrder(createOrderRequest);
    }

    public Order updateOrder(Long orderId, UpdateOrderRequest updateOrderRequest){
        return orderService.updateOrder(orderId, updateOrderRequest);
    }

    public Order cancelOrder(Long orderId){
        return orderService.cancelOrder(orderId);
    }

    public OrderStatus queryOrderStatus(Long orderId){
        return orderService.queryOrderStatus(orderId);
    }

    public List<Order> getPendingOrders(String stockSymbol){
        return orderService.getPendingOrders(stockSymbol);
    }
}
