package com.live_commerce.notification.domain.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Table(name = "p_notifications")
@NoArgsConstructor
public class Notification extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private UUID userId;

  @Enumerated(EnumType.STRING)
  private NotificationType type;

  private UUID targetId;

  private String message;

  private boolean isSent = false; // 전송 여부

  private LocalDateTime sentAt ; // 알림 전송 시간

  private LocalDateTime scheduledAt; // 실제 알림 전송 시간

  public Notification updateIsSent() {
    return Notification.builder()
        .id(this.id)
        .userId(this.userId)
        .type(this.type)
        .targetId(this.targetId)
        .message(this.message)
        .isSent(true)
        .sentAt(LocalDateTime.now())
        .scheduledAt(this.scheduledAt)
        .build();
  }
}
