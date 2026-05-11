package com.live_commerce.payment.infrastructure.scheduler;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.live_commerce.payment.application.service.PaymentTxProcessor;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentMethod;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentExpiredBatchSchedulerTest {

	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private PaymentTxProcessor paymentTxProcessor;

	@InjectMocks
	private PaymentExpiredBatchScheduler scheduler;

	@DisplayName("만료된 PENDING 결제에 대해 fail() 호출")
	@Test
	void expireOverdue_callsFailForExpiredPayments() {
		UUID orderId1 = UUID.randomUUID();
		UUID orderId2 = UUID.randomUUID();
		Payment p1 = expiredPendingPayment(orderId1);
		Payment p2 = expiredPendingPayment(orderId2);
		when(paymentRepository.findByStatusAndExpiresAtBefore(eq(PaymentStatus.PENDING), any()))
			.thenReturn(List.of(p1, p2));

		scheduler.expireOverdue();

		verify(paymentTxProcessor).fail(orderId1, "결제 유효시간 만료(배치)");
		verify(paymentTxProcessor).fail(orderId2, "결제 유효시간 만료(배치)");
	}

	@DisplayName("만료된 결제 없음 - fail() 미호출")
	@Test
	void expireOverdue_noExpiredPayments_noCalls() {
		when(paymentRepository.findByStatusAndExpiresAtBefore(any(), any())).thenReturn(List.of());

		scheduler.expireOverdue();

		verify(paymentTxProcessor, never()).fail(any(), any());
	}

	@DisplayName("개별 fail() 예외 발생해도 나머지 결제 계속 처리")
	@Test
	void expireOverdue_oneFailThrows_continuesOthers() {
		UUID orderId1 = UUID.randomUUID();
		UUID orderId2 = UUID.randomUUID();
		when(paymentRepository.findByStatusAndExpiresAtBefore(eq(PaymentStatus.PENDING), any()))
			.thenReturn(List.of(expiredPendingPayment(orderId1), expiredPendingPayment(orderId2)));
		doThrow(new RuntimeException("DB 오류")).when(paymentTxProcessor).fail(eq(orderId1), any());

		scheduler.expireOverdue();

		verify(paymentTxProcessor).fail(orderId1, "결제 유효시간 만료(배치)");
		verify(paymentTxProcessor).fail(orderId2, "결제 유효시간 만료(배치)");
	}

	private Payment expiredPendingPayment(UUID orderId) {
		Payment payment = Payment.of(UUID.randomUUID(), orderId, BigDecimal.valueOf(5000), PaymentMethod.KAKAO);
		payment.expireAt(LocalDateTime.now().minusMinutes(1));
		return payment;
	}
}
