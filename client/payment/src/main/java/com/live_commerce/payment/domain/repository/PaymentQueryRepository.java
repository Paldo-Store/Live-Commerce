package com.live_commerce.payment.domain.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.live_commerce.payment.application.dto.request.PaymentSearchCondition;
import com.live_commerce.payment.domain.model.Payment;

public interface PaymentQueryRepository {
	List<Payment> searchPayment(PaymentSearchCondition condition, Pageable pageable);

}
