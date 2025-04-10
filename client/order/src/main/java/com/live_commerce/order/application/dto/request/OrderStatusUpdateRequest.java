package com.live_commerce.order.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusUpdateRequest(
        @NotBlank String status // 예: "ORDER_CANCELED", "PAID"
) {
}