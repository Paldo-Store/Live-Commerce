package com.live_commerce.chat.infrastructure.client;

import java.util.UUID;

public record BroadcastStatusResponse(UUID broadcastId, BroadcastStatus broadcastStatus) {
}