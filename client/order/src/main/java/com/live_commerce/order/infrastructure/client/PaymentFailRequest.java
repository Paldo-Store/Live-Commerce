package com.live_commerce.order.infrastructure.client;

public record PaymentFailRequest(
        boolean success,
        String message
) {}