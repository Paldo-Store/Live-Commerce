package com.live_commerce.notification.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Table(name = "p_notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

//  private UUID userId;

  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @Column(nullable = false, unique = true)
  private UUID targetId;

  private String message;

  private boolean isSent = false; // 전송 여부

  private boolean isFailed = false;

  private int retryCount = 0;

  private LocalDateTime scheduledAt; // 알림 예약 시간

  private LocalDateTime sentAt ; // 실제 전송 시간


  // 알림 예약용 생성 메서드 (전송 전)
  public static Notification reserve (NotificationType type, UUID targetId, LocalDateTime scheduledAt) {
    return Notification.builder()
        .type(type)
        .targetId(targetId)
        .message("")
        .isSent(false)
        .isFailed(false)
        .retryCount(0)
        .scheduledAt(scheduledAt)
        .sentAt(null)
        .build();
  }

  // 알림 전송 후 상태 변경
  public Notification markAsSent() {
    return Notification.builder()
        .id(this.id)
        .type(this.type)
        .targetId(this.targetId)
        .message(this.message)
        .isSent(true)
        .isFailed(false)
        .retryCount(this.retryCount)
        .scheduledAt(this.scheduledAt)
        .sentAt(LocalDateTime.now())
        .build();
  }

  public Notification increaseRetryCount() {
    return Notification.builder()
        .id(this.id)
        .type(this.type)
        .targetId(this.targetId)
        .message(this.message)
        .isSent(this.isSent)
        .isFailed(this.isFailed)
        .retryCount(this.retryCount + 1)
        .scheduledAt(this.scheduledAt)
        .sentAt(this.sentAt)
        .build();
  }

  // 재시로 횟수 초과 실패 처리
  public Notification markAsFailed() {
    return Notification.builder()
        .id(this.id)
        .type(this.type)
        .targetId(this.targetId)
        .message(this.message)
        .isSent(true)           // 더 이상 재시도 안 함
        .isFailed(true)         // 실패로 마무리
        .retryCount(this.retryCount)
        .scheduledAt(this.scheduledAt)
        .sentAt(null)           // 전송은 안 됐으므로 null 유지
        .build();
  }




}
