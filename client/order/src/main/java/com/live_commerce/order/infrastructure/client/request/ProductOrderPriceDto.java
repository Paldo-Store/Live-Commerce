package com.live_commerce.order.infrastructure.client.request;

import java.util.UUID;

public record ProductOrderPriceDto (
        UUID productId,
        Integer currentPrice
) {}
