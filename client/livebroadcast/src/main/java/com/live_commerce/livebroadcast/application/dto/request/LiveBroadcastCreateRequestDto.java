package com.live_commerce.livebroadcast.application.dto.request;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LiveBroadcastCreateRequestDto (

        @NotBlank(message = "방송 제목은 필수입니다.") String broadcastName,
        @NotNull(message = "시작 시간은 필수입니다.") LocalDateTime startTime,
        @NotNull(message = "종료 시간은 필수입니다.") LocalDateTime endTime,
        UUID hostId,
        @NotNull(message = "회사 ID는 필수입니다.") UUID companyId
) {}
