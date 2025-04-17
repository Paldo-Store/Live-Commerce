package com.live_commerce.order.infrastructure;

import com.live_commerce.payment.domain.model.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundResponseDTO(
        UUID paymentId,
        UUID orderId,
        String status,
        BigDecimal amount
) {
}