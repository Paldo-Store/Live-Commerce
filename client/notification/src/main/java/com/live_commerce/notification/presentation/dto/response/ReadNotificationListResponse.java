package com.live_commerce.notification.presentation.dto.response;

import java.util.List;

public record ReadNotificationListResponse(
    List<NotificationResponse> notifications
) {

}
