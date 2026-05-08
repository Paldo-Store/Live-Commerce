package com.live_commerce.payment.domain.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_payment_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOutbox {

	@Id
	@UuidGenerator
	private UUID id;

	@Column(nullable = false)
	private UUID orderId;

	@Column(nullable = false)
	private String eventType;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OutboxStatus status;

	@Column(updatable = false, nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime publishedAt;

	@Column(nullable = false)
	private int retryCount = 0;

	public static PaymentOutbox of(UUID orderId, String eventType, String payload) {
		return new PaymentOutbox(orderId, eventType, payload);
	}

	private PaymentOutbox(UUID orderId, String eventType, String payload) {
		this.orderId = orderId;
		this.eventType = eventType;
		this.payload = payload;
		this.status = OutboxStatus.PENDING;
		this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
	}

	public void markPublished() {
		this.status = OutboxStatus.PUBLISHED;
		this.publishedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
	}

	public void markFailed() {
		this.status = OutboxStatus.FAILED;
	}

	public void incrementRetry() {
		this.retryCount++;
	}
}
