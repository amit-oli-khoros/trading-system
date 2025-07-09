package org.amit.service;

import org.amit.common.Logger;
import org.amit.common.TradeIdGenerator;
import org.amit.models.Order;
import org.amit.models.Trade;
import org.amit.store.OrderStore;
import org.amit.store.TradeStore;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class OrderMatcher implements Runnable{
    private final OrderStore orderStore;
    private final TradeStore tradeStore;

    public OrderMatcher(OrderStore orderStore, TradeStore tradeStore) {
        this.orderStore = orderStore;
        this.tradeStore = tradeStore;
    }

    public void matchOrders() {
        Logger.log("In matchOrders");
        List<String> allStockSymbols;
        try {
            allStockSymbols = orderStore.getAllStockSymbols();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(String stock: allStockSymbols) {
            ReentrantLock lock = orderStore.getLock(stock);
            lock.lock();
            try {
                PriorityQueue<Order> buyOrders = orderStore.getBuyOrderBook().get(stock);
                PriorityQueue<Order> sellOrders = orderStore.getSellOrderBook().get(stock);
                while(canMatch(buyOrders, orderStore.getSellOrderBook().get(stock))) {
                    Order buyOrder = buyOrders.poll();
                    Order sellOrder = sellOrders.poll();
                    Long tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                    BigDecimal tradePrice = getTradePrice(buyOrder, sellOrder);

                    createTrade(stock, buyOrder, sellOrder, tradePrice, tradeQuantity);

                    updateOrderBook(buyOrder, sellOrder, tradeQuantity, sellOrders, buyOrders);
                }
            }
            finally {
                lock.unlock();
            }
        }
    }

    private void createTrade(String stock, Order buyOrder, Order sellOrder, BigDecimal tradePrice, Long tradeQuantity) {
        Trade trade = Trade.builder()
                .id(TradeIdGenerator.nextId())
                .stockSymbol(stock)
                .buyerOrderId(buyOrder.getId())
                .sellerOrderId(sellOrder.getId())
                .price(tradePrice)
                .executedAt(Instant.now())
                .quantity(tradeQuantity)
                .build();

        tradeStore.saveTrade(trade);
    }

    private void updateOrderBook(Order buyOrder, Order sellOrder, Long tradeQuantity, PriorityQueue<Order> sellOrders, PriorityQueue<Order> buyOrders) {
        if(buyOrder.getQuantity() > tradeQuantity){
            buyOrders.add(buyOrder.withReducedQuantity(tradeQuantity));
        }
        if(sellOrder.getQuantity() > tradeQuantity){
            sellOrders.add(sellOrder.withReducedQuantity(tradeQuantity));
        }
    }

    private BigDecimal getTradePrice(Order buyOrder, Order sellOrder) {
        if(buyOrder.getOrderPlacedAt().isBefore(sellOrder.getOrderPlacedAt())) {
            return buyOrder.getPrice();
        }
        return sellOrder.getPrice();
    }

    private boolean canMatch(PriorityQueue<Order> buyOrders, PriorityQueue<Order> sellOrders) {
        return !buyOrders.isEmpty() && !sellOrders.isEmpty() && (buyOrders.peek().getPrice().compareTo(sellOrders.peek().getPrice()) >= 0);
    }


    @Override
    public void run() {
        while(true) {
            matchOrders();
            try {
                Thread.sleep(5000);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
