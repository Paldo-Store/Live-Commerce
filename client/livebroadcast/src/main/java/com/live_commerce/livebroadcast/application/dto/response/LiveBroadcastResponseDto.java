package com.live_commerce.livebroadcast.application.dto.response;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LiveBroadcastResponseDto (
        UUID id,
        String broadcastName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BroadcastStatus broadcastStatus,
        UUID hostId,
        UUID companyId,
        Integer totalViewerCount
) { }
