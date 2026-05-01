package com.live_commerce.payment.application.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.domain.event.PaymentCompletedDomainEvent;
import com.live_commerce.payment.domain.event.PaymentFailedDomainEvent;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTxProcessor {

	private final PaymentRepository paymentRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void fail(UUID orderId, String reason) {
		Payment p = findByOrderId(orderId);
		p.fail();
		eventPublisher.publishEvent(new PaymentFailedDomainEvent(p.getOrderId(), reason));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void complete(UUID orderId) {
		Payment p = findByOrderId(orderId);
		p.complete();
		eventPublisher.publishEvent(new PaymentCompletedDomainEvent(p.getOrderId(), p.getAmount()));
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
}
