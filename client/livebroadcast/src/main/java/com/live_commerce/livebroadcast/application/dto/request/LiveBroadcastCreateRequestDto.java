package com.live_commerce.livebroadcast.application.dto.request;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class LiveBroadcastCreateRequestDto {

    private String broadcastName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BroadcastStatus broadcastStatus;

    // 방송 생성하는 사람을 사용자 정보에서 자동 추론
    private UUID hostId;

    private UUID companyId;

    private Integer totalViewerCount;

}
