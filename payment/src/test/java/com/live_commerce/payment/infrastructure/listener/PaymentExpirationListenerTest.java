package com.live_commerce.payment.infrastructure.listener;

import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import com.live_commerce.payment.application.service.PaymentTxProcessor;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentMethod;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.redis.PaymentRedisKeys;

@ExtendWith(MockitoExtension.class)
class PaymentExpirationListenerTest {

	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private PaymentTxProcessor paymentTxProcessor;

	@InjectMocks
	private PaymentExpirationListener listener;

	@DisplayName("올바른 키 prefix + PENDING 결제 존재 시 fail() 호출")
	@Test
	void onMessage_validKey_callsFail() {
		UUID orderId = UUID.randomUUID();
		String key = PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId;
		Payment payment = Payment.of(UUID.randomUUID(), orderId, java.math.BigDecimal.valueOf(5000), PaymentMethod.KAKAO);
		when(paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING))
			.thenReturn(Optional.of(payment));

		listener.onMessage(messageOf(key), null);

		verify(paymentTxProcessor).fail(orderId, "결제 유효시간 만료");
	}

	@DisplayName("잘못된 키 prefix - fail() 미호출")
	@Test
	void onMessage_invalidPrefix_ignored() {
		listener.onMessage(messageOf("other:key:abc"), null);

		verify(paymentTxProcessor, never()).fail(any(), any());
	}

	@DisplayName("PENDING 결제 없음 - fail() 미호출")
	@Test
	void onMessage_noPayment_ignored() {
		UUID orderId = UUID.randomUUID();
		when(paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING))
			.thenReturn(Optional.empty());

		listener.onMessage(messageOf(PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId), null);

		verify(paymentTxProcessor, never()).fail(any(), any());
	}

	@DisplayName("fail() 에서 IllegalStateException 발생해도 예외 전파 없음")
	@Test
	void onMessage_failThrows_noException() {
		UUID orderId = UUID.randomUUID();
		Payment payment = Payment.of(UUID.randomUUID(), orderId, java.math.BigDecimal.valueOf(5000), PaymentMethod.KAKAO);
		when(paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING))
			.thenReturn(Optional.of(payment));
		doThrow(new IllegalStateException("이미 처리됨")).when(paymentTxProcessor).fail(any(), any());

		listener.onMessage(messageOf(PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId), null);
		// 예외 없이 정상 종료
	}

	private Message messageOf(String key) {
		Message message = mock(Message.class);
		when(message.getBody()).thenReturn(key.getBytes());
		return message;
	}
}
