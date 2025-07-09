package org.amit.models;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class Trade {
    private Long id;
    private Long buyerOrderId;
    private Long sellerOrderId;
    private String stockSymbol;
    private Long quantity;
    private BigDecimal price;
    private Instant executedAt;
}
