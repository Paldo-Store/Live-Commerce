package com.live_commerce.chat.application.dto.response;

import java.util.UUID;

//TODO feign 응답 타입도 동일해야함.
public record BroadcastResponse(
        UUID LiveBroadcastId,
        String broadcastName,
        String broadcastStatus) {  //enum 대신 String으로 받는다.
}