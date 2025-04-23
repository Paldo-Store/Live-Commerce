package com.live_commerce.order.infrastructure;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundResponseDTO(
        UUID paymentId,
        UUID orderId,
        String status,
        BigDecimal amount
) {
}