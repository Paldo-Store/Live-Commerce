package com.live_commerce.order.kafkaOrder;

import com.live_commerce.order.infrastructure.client.feignEnum.BroadcastStatus;

//방송 상태의 응답 메시지
//broadcast -> order
public record BroadcastStatusResponseMessage(
        String requestId,
        BroadcastStatus broadcastStatus
) {
}