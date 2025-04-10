package com.live_commerce.order.infrastructure.client.response;

import lombok.Getter;

@Getter
public class BroadcastStatusResponse {
    private String broadcastStatus; // LIVE, ENDED, SCHEDULED 중 하나
}
