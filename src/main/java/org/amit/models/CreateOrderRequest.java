package org.amit.models;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CreateOrderRequest {
    private Long userId;
    private OrderType orderType;
    private String stockSymbol;
    private Long quantity;
    private BigDecimal price;
}
