package com.live_commerce.order.infrastructure.client.request;

public record PaymentFailRequest(
        boolean success,
        String message
) {}