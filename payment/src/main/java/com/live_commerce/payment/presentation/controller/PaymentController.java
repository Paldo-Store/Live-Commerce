package com.live_commerce.payment.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.live_commerce.payment.application.dto.request.PaymentApproveRequestDto;
import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.dto.response.PaymentRefundResponseDto;
import com.live_commerce.payment.application.dto.request.PaymentSearchCondition;
import com.live_commerce.payment.application.dto.response.PaymentApproveResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentGetResponseDto;
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
	public ResponseEntity<ApiResponse<PaymentApproveResponseDto>> approvePayment(
		@AuthenticationPrincipal RequestUserDetails userDetails,
		@RequestBody PaymentApproveRequestDto requestDto
	) {
		PaymentApproveResponseDto response = paymentService.approvePayment(
			requestDto,
			userDetails.getUserId()
		);
		return ResponseUtil.success(response);
	}

	@GetMapping("/{paymentId}")
	public ResponseEntity<ApiResponse<PaymentGetResponseDto>> getPayment(
		@PathVariable UUID paymentId,
		@AuthenticationPrincipal RequestUserDetails userDetails
	) {
		PaymentGetResponseDto response = paymentService.getPayment(paymentId, userDetails);
		return ResponseUtil.success(response);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<PaymentGetResponseDto>>> getPayments(
		@ModelAttribute PaymentSearchCondition condition,
		@AuthenticationPrincipal RequestUserDetails userDetails,
		@PageableDefault(size = 10) Pageable pageable
	) {
		Page<PaymentGetResponseDto> result = paymentService.getPayments(condition, userDetails, pageable);
		return ResponseUtil.success(result);
	}

	@PostMapping("/{orderId}/refund")
	public ResponseEntity<ApiResponse<PaymentRefundResponseDto>> refundPayment(
		@PathVariable UUID orderId,
		@AuthenticationPrincipal RequestUserDetails userDetails
	) {
		PaymentRefundResponseDto response = paymentService.refundPaymentByOrderId(orderId, userDetails);
		return ResponseUtil.success(response);
	}

	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<ApiResponse<Void>> cancelPayment(
		@PathVariable UUID orderId,
		@AuthenticationPrincipal RequestUserDetails userDetails
	) {
		paymentService.cancelPaymentByOrderId(orderId, userDetails);
		return ResponseUtil.noContent();
	}

}
