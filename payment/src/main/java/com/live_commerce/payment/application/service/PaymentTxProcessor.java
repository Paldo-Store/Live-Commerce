package com.live_commerce.payment.application.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentOutbox;
import com.live_commerce.payment.domain.repository.PaymentOutboxRepository;
import com.live_commerce.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTxProcessor {

	private final PaymentRepository paymentRepository;
	private final PaymentOutboxRepository paymentOutboxRepository;
	private final ObjectMapper objectMapper;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void fail(UUID orderId, String reason) {
		Payment p = findByOrderId(orderId);
		p.fail();
		paymentOutboxRepository.save(PaymentOutbox.of(orderId, "PAYMENT_FAILED", buildFailedPayload(orderId, reason)));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void complete(UUID orderId) {
		Payment p = findByOrderId(orderId);
		p.complete();
		paymentOutboxRepository.save(PaymentOutbox.of(orderId, "PAYMENT_COMPLETED", buildCompletedPayload(p)));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Payment refund(UUID orderId) {
		Payment p = findByOrderId(orderId);
		p.refund();
		return p;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void cancel(UUID orderId) {
		Payment p = findByOrderId(orderId);
		p.cancel();
	}

	private Payment findByOrderId(UUID orderId) {
		return paymentRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
	}

	private String buildCompletedPayload(Payment p) {
		try {
			return objectMapper.writeValueAsString(Map.of(
				"orderId", p.getOrderId().toString(),
				"amount", p.getAmount().toPlainString()
			));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("outbox payload 직렬화 실패: orderId=" + p.getOrderId(), e);
		}
	}

	private String buildFailedPayload(UUID orderId, String reason) {
		try {
			return objectMapper.writeValueAsString(Map.of(
				"orderId", orderId.toString(),
				"message", reason
			));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("outbox payload 직렬화 실패: orderId=" + orderId, e);
		}
	}
}
