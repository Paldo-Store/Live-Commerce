package com.live_commerce.livebroadcast.application.dto.response;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LiveBroadcastResponseDto (
        UUID liveBroadcastId,
        String broadcastName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BroadcastStatus broadcastStatus,
        Integer totalViewerCount,
        UUID hostId,
        UUID companyId
) {}
