package com.live_commerce.livebroadcast.application.dto.response;

import java.util.List;
import java.util.UUID;

public record SubscriptionUserListResponseDto (
        List<UUID> userIds
) {
}
