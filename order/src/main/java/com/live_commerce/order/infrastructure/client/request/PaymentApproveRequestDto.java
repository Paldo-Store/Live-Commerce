package com.live_commerce.order.infrastructure.client.request;

import java.util.UUID;

public record PaymentApproveRequestDto(
        String tid,
        String pgToken,
        UUID orderId
) {}