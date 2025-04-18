package com.live_commerce.notification.presentation.dto.request;

import java.util.List;

public record BroadcastNotificationContext(
    List<UserInfo> users
) {

}
