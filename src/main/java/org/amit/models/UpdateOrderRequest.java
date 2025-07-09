package org.amit.models;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class UpdateOrderRequest {
    private Long quantity;
    private BigDecimal price;
}
