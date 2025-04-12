package com.live_commerce.livebroadcast.application.dto.response;

import java.util.UUID;

public record BroadcastProductListResponseDto (
        UUID productId,
        String name,
        Integer price
) {
}
