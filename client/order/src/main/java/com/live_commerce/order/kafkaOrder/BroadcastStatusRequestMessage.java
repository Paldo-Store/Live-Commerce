package com.live_commerce.order.kafkaOrder;

import java.util.UUID;

//order -> broadcast
// 방송 상태 요청 메시지 클래스
public record BroadcastStatusRequestMessage(
        UUID broadcastId
) {
}