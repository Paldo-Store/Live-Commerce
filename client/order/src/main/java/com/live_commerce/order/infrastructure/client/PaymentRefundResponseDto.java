package com.live_commerce.order.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundResponseDto(
        UUID paymentId,
        UUID orderId,
        String status,
        BigDecimal amount
) {
}
