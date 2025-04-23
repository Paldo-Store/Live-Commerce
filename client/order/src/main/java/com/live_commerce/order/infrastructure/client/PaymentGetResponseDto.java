package com.live_commerce.order.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentGetResponseDto(
        UUID paymentId,
        UUID orderId,
        UUID userId,
        BigDecimal amount,
        PaymentStatus status,
        String tid
) {}
