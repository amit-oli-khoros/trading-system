package org.amit;

import org.amit.common.Logger;
import org.amit.models.*;
import org.amit.processors.OrderProcessor;
import org.amit.service.OrderMatcher;
import org.amit.service.OrderService;
import org.amit.store.InMemoryOrderStore;
import org.amit.store.InMemoryTradeStore;
import org.amit.store.OrderStore;
import org.amit.store.TradeStore;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SequentialMain {
    public static void main(String[] args) throws InterruptedException {
        OrderStore orderStore = new InMemoryOrderStore();
        TradeStore tradeStore = new InMemoryTradeStore();
        OrderService orderService = new OrderService(orderStore);
        OrderProcessor orderProcessor = new OrderProcessor(orderService);

        User user1 = User.builder()
                .id(1L)
                .name("Amit")
                .email("amit@gmail.com")
                .phoneNumber("9876543210")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("Alok")
                .email("alok@gmail.com")
                .phoneNumber("9876543211")
                .build();
        User user3 = User.builder()
                .id(3L)
                .name("Alka")
                .email("alka@gmail.com")
                .phoneNumber("9876543212")
                .build();

        CreateOrderRequest buyOrder = CreateOrderRequest.builder()
                .userId(user1.getId())
                .stockSymbol("INFY")
                .orderType(OrderType.BUY)
                .quantity(10L)
                .price(BigDecimal.valueOf(100))
                .build();
        orderProcessor.placeOrder(buyOrder);

        CreateOrderRequest sellOrder = CreateOrderRequest.builder()
                .userId(user2.getId())
                .stockSymbol("INFY")
                .orderType(OrderType.SELL)
                .quantity(10L)
                .price(BigDecimal.valueOf(100))
                .build();
        orderProcessor.placeOrder(sellOrder);

        CreateOrderRequest buyOrderForUser3 = CreateOrderRequest.builder()
                .userId(user3.getId())
                .stockSymbol("OFSS")
                .orderType(OrderType.BUY)
                .quantity(9L)
                .price(BigDecimal.valueOf(1000))
                .build();
        Order createdOrder1 = orderProcessor.placeOrder(buyOrderForUser3);
        orderProcessor.cancelOrder(createdOrder1.getId());

        CreateOrderRequest buyOrderForUser3_1 = CreateOrderRequest.builder()
                .userId(user3.getId())
                .stockSymbol("OFSS")
                .orderType(OrderType.BUY)
                .quantity(9L)
                .price(BigDecimal.valueOf(1000))
                .build();
        Order createdOrder2 = orderProcessor.placeOrder(buyOrderForUser3_1);
        orderProcessor.updateOrder(createdOrder2.getId(), UpdateOrderRequest.builder().quantity(5L).price(BigDecimal.valueOf(900)).build());

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        executorService.submit(new OrderMatcher(orderStore, tradeStore));


        Thread.sleep(5000);
        Logger.log(orderProcessor.getPendingOrders("INFY").toString());


        Logger.log("buyOrder size of priority queue "+ orderStore.getBuyOrderBook().values().stream().mapToInt(Collection::size).sum());
        Logger.log("buyOrder book "+ orderStore.getBuyOrderBook());
        Logger.log("sellOrder book size of priority queue "+ orderStore.getSellOrderBook().values().stream().mapToInt(Collection::size).sum());
        Logger.log("sellOrder book "+ orderStore.getSellOrderBook());
        Logger.log("trade book "+ tradeStore.getAllTrades());
        Logger.log(orderProcessor.queryOrderStatus(3L).toString());

    }
}
