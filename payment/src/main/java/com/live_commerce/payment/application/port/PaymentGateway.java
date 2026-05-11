package com.live_commerce.payment.application.port;

import com.live_commerce.payment.application.port.dto.PaymentApproveResult;
import com.live_commerce.payment.application.port.dto.PaymentReadyResult;
import com.live_commerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {

	boolean supports(PaymentMethod method);

	PaymentReadyResult ready(UUID userId, UUID orderId, BigDecimal amount, String itemName);

	PaymentApproveResult approve(String tid, String pgToken, String orderId, UUID userId, BigDecimal amount);

	void cancel(String tid, BigDecimal cancelAmount);
}
