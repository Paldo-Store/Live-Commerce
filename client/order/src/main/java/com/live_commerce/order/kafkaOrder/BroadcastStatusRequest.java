package com.live_commerce.order.kafkaOrder;


import java.util.UUID;

// order -> broadcast
// 주문 측에서 방송아이디를 얻기위해 broadcast로 요청 보낸다.
public record BroadcastStatusRequest(
        UUID broadcastId
) {
}