package com.live_commerce.livebroadcast.infrastructure.client.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProductSummaryDto (
        UUID id,
        String name,
        Integer price
) {
}
