package com.live_commerce.order.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSuccessRequest(
        UUID paymentId,
        boolean success,
        String message,
        BigDecimal finalPaidPrice
) {}