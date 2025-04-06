package com.live_commerce.livebroadcast.application.dto;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@ToString
public class LiveBroadcastCreateResponseDto {

    private UUID id;

    private String broadcastName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BroadcastStatus broadcastStatus;

    private UUID hostId;

    private UUID companyId;

    private Integer totalViewerCount;

}
