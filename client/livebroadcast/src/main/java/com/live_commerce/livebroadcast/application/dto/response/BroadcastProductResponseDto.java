package com.live_commerce.livebroadcast.application.dto.response;

import lombok.Builder;

import java.util.UUID;

public record BroadcastProductResponseDto (
        UUID id,
        UUID broadcastId,
        UUID productId
){
}
