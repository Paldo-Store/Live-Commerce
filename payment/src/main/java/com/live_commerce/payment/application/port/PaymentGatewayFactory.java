package com.live_commerce.payment.application.port;

import java.util.List;

import org.springframework.stereotype.Component;

import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.domain.model.PaymentMethod;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

	private final List<PaymentGateway> gateways;

	public PaymentGateway getGateway(PaymentMethod method) {
		return gateways.stream()
			.filter(g -> g.supports(method))
			.findFirst()
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.UNSUPPORTED_PAYMENT_METHOD));
	}
}
