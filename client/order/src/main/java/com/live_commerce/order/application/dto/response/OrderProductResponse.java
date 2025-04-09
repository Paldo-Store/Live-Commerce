package com.live_commerce.order.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductResponse {
    private UUID productId;
    private String productName;
    private Integer productQuantity;
    private Long productTotalPrice;
    private Boolean deletedStatus;
}
