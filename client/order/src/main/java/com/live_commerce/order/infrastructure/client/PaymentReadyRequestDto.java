package com.live_commerce.order.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentReadyRequestDto(
        UUID orderId,
        double amount,
        String itemName
) {}
