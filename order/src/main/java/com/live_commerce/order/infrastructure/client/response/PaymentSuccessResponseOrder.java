package com.live_commerce.order.infrastructure.client.response;

import java.util.UUID;

public record PaymentSuccessResponseOrder(
        UUID orderId,
        boolean success) {}