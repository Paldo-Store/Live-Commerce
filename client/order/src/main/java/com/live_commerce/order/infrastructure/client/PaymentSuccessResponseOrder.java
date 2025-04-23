package com.live_commerce.order.infrastructure.client;

import java.util.UUID;

public record PaymentSuccessResponseOrder(
        UUID orderId,
        boolean success) {}