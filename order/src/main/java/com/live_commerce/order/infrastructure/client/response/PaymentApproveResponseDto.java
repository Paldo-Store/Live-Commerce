package com.live_commerce.order.infrastructure.client.response;

import java.math.BigDecimal;

public record PaymentApproveResponseDto(
        String tid,
        java.time.LocalDateTime approvedAt,
        BigDecimal amount
) {
}
