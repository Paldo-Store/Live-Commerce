package com.live_commerce.notification.presentation.dto.request;

import com.live_commerce.notification.domain.model.NotificationType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationCreateRequest(
    @NotNull(message = "알림 타입을 지정해 주세요.") NotificationType notificationType,
    @NotNull(message = "전송하는 서비스의 ID를 입력해 주세요.")UUID targetId,
    @NotNull(message = "알림 시간을 설정해 주세요.")LocalDateTime scheduledAt
) {
}
