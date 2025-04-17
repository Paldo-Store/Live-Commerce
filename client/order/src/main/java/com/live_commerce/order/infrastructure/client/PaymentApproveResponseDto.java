package com.live_commerce.order.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PaymentApproveResponseDto(
        String tid,
        java.time.LocalDateTime approvedAt,
        BigDecimal amount
) {
}
