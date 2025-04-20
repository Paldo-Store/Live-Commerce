package com.live_commerce.order.infrastructure.client.response;

import com.live_commerce.order.infrastructure.client.BroadcastStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class BroadcastStatusResponse {
    private UUID broadcastId;
    private BroadcastStatus broadcastStatus; // LIVE, ENDED, SCHEDULED 중 하나
}
