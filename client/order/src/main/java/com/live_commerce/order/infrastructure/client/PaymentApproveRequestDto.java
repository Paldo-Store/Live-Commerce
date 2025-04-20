package com.live_commerce.order.infrastructure.client;

import java.util.UUID;

public record PaymentApproveRequestDto(
        String tid,
        String pgToken,
        UUID orderId
) {}