package org.amit;

import org.amit.common.Logger;
import org.amit.models.CreateOrderRequest;
import org.amit.models.OrderType;
import org.amit.models.User;
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
import java.util.concurrent.Future;

public class ParallelMain {
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

        Runnable ordersFromUser1 = () -> {
            for(int i =0;i<5;i++) {
                CreateOrderRequest buyOrder = CreateOrderRequest.builder()
                        .userId(user1.getId())
                        .stockSymbol("INFY")
                        .orderType(OrderType.BUY)
                        .quantity(10L)
                        .price(BigDecimal.valueOf(100 + i))
                        .build();
                orderProcessor.placeOrder(buyOrder);
            }
        };

        Runnable ordersFromUser2 = () -> {
            for(int i =0;i<5;i++) {
                CreateOrderRequest sellOrder = CreateOrderRequest.builder()
                        .userId(user2.getId())
                        .stockSymbol("INFY")
                        .orderType(OrderType.SELL)
                        .quantity(10L)
                        .price(BigDecimal.valueOf(100 + i))
                        .build();
                orderProcessor.placeOrder(sellOrder);
            }
        };

        Runnable ordersFromUser3 = () -> {
            for(int i =0;i<5;i++) {
                CreateOrderRequest sellOrder = CreateOrderRequest.builder()
                        .userId(user3.getId())
                        .stockSymbol("OFSS")
                        .orderType(OrderType.BUY)
                        .quantity(9L)
                        .price(BigDecimal.valueOf(1000 + i))
                        .build();
                orderProcessor.placeOrder(sellOrder);
            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.submit(new OrderMatcher(orderStore, tradeStore));

        Future<?> ordersFromUser1Future = executorService.submit(ordersFromUser1);
        Future<?> ordersFromUser2Future = executorService.submit(ordersFromUser2);
        Future<?> ordersFromUser3Future = executorService.submit(ordersFromUser3);

        Thread.sleep(5000);
        Logger.log("buyOrder size of priority queue "+ orderStore.getBuyOrderBook().values().stream().mapToInt(Collection::size).sum());
        Logger.log("buyOrder book "+ orderStore.getBuyOrderBook());
        Logger.log("sellOrder book size of priority queue "+ orderStore.getSellOrderBook().values().stream().mapToInt(Collection::size).sum());
        Logger.log("sellOrder book "+ orderStore.getSellOrderBook());
        Logger.log("trade book "+ tradeStore.getAllTrades());
    }
}