package com.live_commerce.order.infrastructure.client.request;

import java.math.BigDecimal;

public record PaymentSuccessRequest(
        boolean success,
        String message,
        BigDecimal finalPaidPrice
) {}