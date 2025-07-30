package com.live_commerce.livebroadcast.application.dto.request;

import java.util.UUID;

public record CreateSubscriptionRequestDto (
        UUID broadcastId
) {
}
