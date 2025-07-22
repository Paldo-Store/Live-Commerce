package com.live_commerce.livebroadcast.application.dto.request;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record LiveBroadcastUpdateRequestDto(
        String broadcastName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BroadcastStatus broadcastStatus
) {}
