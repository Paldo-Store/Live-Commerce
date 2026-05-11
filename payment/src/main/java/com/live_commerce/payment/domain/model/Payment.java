package com.live_commerce.payment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_payment")
public class Payment extends BaseEntity {

	@Id
	@UuidGenerator
	private UUID id;

	@Column(nullable = false, unique = true)
	private UUID orderId;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;

	private String tid;

	private LocalDateTime expiresAt;

	public void assignTid(String tid) {
		this.tid = tid;
	}

	public void expireAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public void complete() {
		validateTransition(PaymentStatus.COMPLETED);
		this.status = PaymentStatus.COMPLETED;
	}

	public void fail() {
		validateTransition(PaymentStatus.FAILED);
		this.status = PaymentStatus.FAILED;
	}

	public void cancel() {
		validateTransition(PaymentStatus.CANCELED);
		this.status = PaymentStatus.CANCELED;
	}

	public void refund() {
		validateTransition(PaymentStatus.REFUND);
		this.status = PaymentStatus.REFUND;
	}

	private void validateTransition(PaymentStatus next) {
		if (!status.canTransitionTo(next)) {
			throw new IllegalStateException("유효하지 않은 상태 전이: " + status + " → " + next);
		}
	}

	public void updateStatus(PaymentStatus newStatus) {
		this.status = newStatus;
	}

	// 정적 팩토리 메서드
	public static Payment of(UUID userId, UUID orderId, BigDecimal amount, PaymentMethod paymentMethod) {
		return new Payment(userId, orderId, amount, PaymentStatus.PENDING, paymentMethod);
	}

	private Payment(UUID userId, UUID orderId, BigDecimal amount, PaymentStatus status, PaymentMethod paymentMethod) {
		this.userId = userId;
		this.orderId = orderId;
		this.amount = amount;
		this.status = status;
		this.paymentMethod = paymentMethod;
	}

}

