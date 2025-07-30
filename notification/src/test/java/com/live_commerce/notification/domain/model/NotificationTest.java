package com.live_commerce.notification.domain.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static com.live_commerce.notification.domain.model.NotificationType.LIVE_BROADCAST;

import com.live_commerce.notification.application.service.NotificationService;
import com.live_commerce.notification.domain.repository.NotificationRepository;
import com.live_commerce.notification.presentation.dto.request.NotificationCreateRequest;
import com.live_commerce.notification.presentation.dto.response.NotificationCreateResponse;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
public class NotificationTest {

  @InjectMocks
  private NotificationService notificationService;

  @Mock
  private NotificationRepository notificationRepository;

  @Test
  @DisplayName("알림 생성 기능 테스트 - 알림이 정상적으로 생성")
  void createNotificationForLiveBroadcast() {
    // given
    UUID targetId = UUID.randomUUID();
    NotificationCreateRequest request = new NotificationCreateRequest(
        LIVE_BROADCAST, targetId, LocalDateTime.now());

    Notification notification = Notification.reserve(request.notificationType(), request.targetId(), request.scheduledAt());
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    // when
    NotificationCreateResponse response = notificationService.createNotificationForLiveBroadcast(request);

    // then
    assertNotNull(response);
    assertEquals(targetId, response.targetId());
  }

  @Test
  @DisplayName("알림 삭제 기능 테스트 - 알림이 존재할 경우 삭제")
  void deleteNotification() {
    // given
    UUID targetId = UUID.randomUUID();
    NotificationCreateRequest request = new NotificationCreateRequest(
        LIVE_BROADCAST, targetId, LocalDateTime.now()
    );

    Notification notification = Notification.reserve(request.notificationType(), request.targetId(), request.scheduledAt());

    // Mock 설정
    when(notificationRepository.findByTargetIdAndDeletedStatusFalse(targetId))
        .thenReturn(Optional.of(notification));

    // when
    notificationService.deleteNotification(targetId);

    // then
    assertThat(notification.getDeletedStatus()).isTrue();
  }

  @Test
  @DisplayName("알림 삭제 기능 테스트 - 알림이 없을 경우 예외 발생")
  void deleteNotificationNotFound() {
    // given
    UUID targetId = UUID.randomUUID();

    when(notificationRepository.findByTargetIdAndDeletedStatusFalse(targetId))
        .thenReturn(Optional.empty());

    // when, then
    assertThrows(NoSuchElementException.class, () -> {
      notificationService.deleteNotification(targetId);
    });
  }
}
