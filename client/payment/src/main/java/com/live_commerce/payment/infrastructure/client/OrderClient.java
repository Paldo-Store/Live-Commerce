package com.live_commerce.payment.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.live_commerce.payment.infrastructure.client.dto.PaymentCancelRequest;
import com.live_commerce.payment.infrastructure.client.dto.PaymentFailRequest;
import com.live_commerce.payment.infrastructure.client.dto.PaymentSuccessRequest;

@FeignClient(name = "order", url = "http://localhost:19091", path = "/api/v1/orders")
public interface OrderClient {

	@PostMapping("/{orderId}/payment-success")
	void notifyPaymentSuccess(
		@PathVariable UUID orderId,
		@RequestBody PaymentSuccessRequest request
	);

	@PostMapping("/{orderId}/payment-fail")
	void notifyPaymentFail(
		@PathVariable UUID orderId,
		@RequestBody PaymentFailRequest request
	);

	@PostMapping("/{orderId}/payment-cancel")
	void notifyOrderCancel(
		@PathVariable UUID orderId,
		@RequestBody PaymentCancelRequest request
	);

}
