package com.live_commerce.payment.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.live_commerce.payment.application.dto.request.PaymentApproveRequestDto;
import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.dto.response.PaymentApproveResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentReadyResponseDto;
import com.live_commerce.payment.application.service.PaymentService;
import com.live_commerce.payment.infrastructure.common.ResponseUtil;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;
import com.live_commerce.payment.presentation.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/ready")
	public ResponseEntity<ApiResponse<PaymentReadyResponseDto>> readyPayment(
		@AuthenticationPrincipal RequestUserDetails requestUserDetails,
		@RequestBody PaymentReadyRequestDto requestDto
	) {
		PaymentReadyResponseDto response = paymentService.readyPayment(requestUserDetails, requestDto);
		return ResponseUtil.success(response);
	}

	@PostMapping("/approve")
	public ResponseEntity<ApiResponse<PaymentApproveResponseDto>> approvePaymentPost(
		@AuthenticationPrincipal RequestUserDetails userDetails,
		@RequestBody PaymentApproveRequestDto requestDto
	) {
		PaymentApproveResponseDto response = paymentService.approvePayment(
			requestDto,
			userDetails.getUserId()
		);
		return ResponseUtil.success(response);
	}


}
