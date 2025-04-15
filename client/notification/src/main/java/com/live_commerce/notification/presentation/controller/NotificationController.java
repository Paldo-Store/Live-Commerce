package com.live_commerce.notification.presentation.controller;

import com.live_commerce.notification.application.service.NotificationService;
import com.live_commerce.notification.infrastructure.common.ResponseUtil;
import com.live_commerce.notification.presentation.common.ApiResponse;
import com.live_commerce.notification.presentation.dto.request.NotificationCreateRequest;
import com.live_commerce.notification.presentation.dto.response.NotificationCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  // 방송 시작 30분 전에 알림을 예약하는 API
  @PostMapping("/create-for-broadcast")
  public ResponseEntity<ApiResponse<NotificationCreateResponse>> createNotificationForLiveBroadcast(
      @RequestBody NotificationCreateRequest request) {
    NotificationCreateResponse response = notificationService.createNotificationForLiveBroadcast(
        request.userId(),
        request.broadcastId(),
        request.notificationTime()
    );

    return ResponseUtil.success(response);
  }
}
