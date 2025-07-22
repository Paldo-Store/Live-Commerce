package com.live_commerce.notification.presentation.controller;

import com.live_commerce.notification.application.service.NotificationService;
import com.live_commerce.notification.infrastructure.common.ResponseUtil;
import com.live_commerce.notification.presentation.common.ApiResponse;
import com.live_commerce.notification.presentation.dto.request.NotificationCreateRequest;
import com.live_commerce.notification.presentation.dto.response.NotificationCreateResponse;
import com.live_commerce.notification.presentation.dto.response.ReadNotificationListResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  // 방송 알림 등록
  @PostMapping("/broadcasts")
  public ResponseEntity<ApiResponse<NotificationCreateResponse>> createNotificationForLiveBroadcast(
      @Valid @RequestBody NotificationCreateRequest request) {
    NotificationCreateResponse response = notificationService.createNotificationForLiveBroadcast(
        request);
    return ResponseUtil.success(response);
  }

  // 알림 목록 조회
  @GetMapping
  public ResponseEntity<ApiResponse<ReadNotificationListResponse>> getAllNotifications() {
    ReadNotificationListResponse response = notificationService.getAllNotifications();
    return ResponseUtil.success(response);
  }

  @DeleteMapping("/{targetId}")
  public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable UUID targetId){

    notificationService.deleteNotification(targetId);
    return ResponseUtil.success("알림 삭제가 성공적으로 완료되었습니다.");
  }

  // 직접 스케줄러 호출을 위한 API 추가 (테스트용)
  @PostMapping("/trigger-scheduled-notifications")
  public ResponseEntity<ApiResponse<String>> triggerScheduledNotifications() throws IOException {
    notificationService.triggerScheduledNotifications();  // 메서드 호출
    return ResponseUtil.success("🔥🔥🔥"+"테스트 알림 호출!");
  }

  // 직접 스케줄러 호출을 위한 API 추가 (테스트용, kafka)
  @PostMapping("/trigger-kafka-notifications")
  public ResponseEntity<ApiResponse<String>> triggerKafkaNotifications() throws IOException {
   notificationService.triggerKafkaNotifications();
   return ResponseUtil.success("kafka 알림 발생 정상 트리거 성공");
  }


}
