package com.live_commerce.payment.domain.model;

import java.math.BigDecimal;
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

	@Column(nullable = false)
	private UUID orderId;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	private String tid; // 카카오페이 결제 시도 후 발급받을 TID

	public void assignTid(String tid) {
		this.tid = tid;
	}

	// 상태 변경 메서드
	public void updateStatus(PaymentStatus newStatus) {
		this.status = newStatus;
	}

	// 정적 팩토리 메서드
	public static Payment of(UUID userId, UUID orderId, BigDecimal amount) {
		return new Payment(userId, orderId, amount, PaymentStatus.PENDING);
	}

	// 프라이빗 생성자
	private Payment(UUID userId, UUID orderId, BigDecimal amount, PaymentStatus status) {
		this.userId = userId;
		this.orderId = orderId;
		this.amount = amount;
		this.status = status;
	}

}

