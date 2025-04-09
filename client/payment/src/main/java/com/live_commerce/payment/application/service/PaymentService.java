package com.live_commerce.payment.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.payment.application.dto.request.PaymentApproveRequestDto;
import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.dto.response.PaymentApproveResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentGetResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentReadyResponseDto;
import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final KakaoPayClient kakaoPayClient;

	@Transactional
	public PaymentReadyResponseDto readyPayment(RequestUserDetails requestUserDetails, PaymentReadyRequestDto requestDto) {
		KakaoPayReadyDto readyDto = kakaoPayClient.requestKakaoPayReady(
			requestUserDetails.getUserId(),
			requestDto.orderId(),
			requestDto.amount()
		);

		Payment payment = requestDto.toEntity(requestUserDetails.getUserId());
		payment.assignTid(readyDto.tid());
		paymentRepository.save(payment);

		return PaymentReadyResponseDto.from(readyDto);
	}

	@Transactional
	public PaymentApproveResponseDto approvePayment(PaymentApproveRequestDto requestDto, UUID userId) {
		KakaoPayApproveDto approveDto = kakaoPayClient.requestKakaoPayApprove(
			requestDto.tid(),
			requestDto.pgToken(),
			requestDto.orderId(),
			userId.toString()
		);

		Payment payment = paymentRepository.findByOrderId(UUID.fromString(requestDto.orderId()))
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));

		payment.updateStatus(PaymentStatus.COMPLETED);

		return PaymentApproveResponseDto.from(approveDto);
	}

	@Transactional(readOnly = true)
	public PaymentGetResponseDto getPayment(UUID paymentId, RequestUserDetails userDetails) {
		Payment payment = findPaymentById(paymentId);
		validatePaymentGetPermission(payment, userDetails);

		return PaymentGetResponseDto.from(payment);
	}

	private boolean hasMasterRole(RequestUserDetails userDetails) {
		return userDetails.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_MASTER"));
	}

	private void validatePaymentGetPermission(Payment payment, RequestUserDetails userDetails) {
		if (!payment.getUserId().equals(userDetails.getUserId()) && !hasMasterRole(userDetails)) {
			throw new CustomException(PaymentExceptionCode.UNAUTHORIZED);
		}
	}

	private Payment findPaymentById(UUID paymentId) {
		return paymentRepository.findById(paymentId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
	}


}