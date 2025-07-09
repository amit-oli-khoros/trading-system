package org.amit.models;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;

@Value
@Builder(toBuilder = true)
public class Order {
    private Long id;
    private Long userId;
    private OrderType orderType;
    private String stockSymbol;
    private Long quantity;
    private BigDecimal price;
    private Instant orderPlacedAt;
    private OrderStatus orderStatus;

    public static final Comparator<Order> BUY_ORDER_COMPARATOR = Comparator.comparing(Order::getPrice, Comparator.reverseOrder())
            .thenComparing(Order::getOrderPlacedAt);
    public static final Comparator<Order> SELL_ORDER_COMPARATOR = Comparator.comparing(Order::getPrice)
            .thenComparing(Order::getOrderPlacedAt);

    public Order withReducedQuantity(Long quantity) {
        if(quantity<=0) {
            return this;
        }
        return this.toBuilder()
                .quantity(this.quantity - quantity)
                .build();
    }

    public Order withUpdatedQuantity(Long quantity) {
        if(quantity < 0) {
            throw new IllegalStateException("Order quantity cannot be less than zero");
        }
        return this.toBuilder()
                .quantity(quantity)
                .build();
    }

    public Order withUpdatedPrice(BigDecimal price) {
        if(price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Order price cannot be less than or equal to zero");
        }
        return this.toBuilder()
                .price(price)
                .build();
    }

    public Order withUpdatedStatus(OrderStatus orderStatus) {
        return this.toBuilder()
                .orderStatus(orderStatus)
                .build();
    }
}
