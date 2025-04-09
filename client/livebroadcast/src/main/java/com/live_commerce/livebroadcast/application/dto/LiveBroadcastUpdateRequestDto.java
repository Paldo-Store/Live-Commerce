package com.live_commerce.livebroadcast.application.dto;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;

import java.time.LocalDateTime;

public record LiveBroadcastUpdateRequestDto(
        String broadcastName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BroadcastStatus broadcastStatus
) {}
