package com.live_commerce.payment.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.payment.application.dto.request.PaymentApproveRequestDto;
import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.dto.response.PaymentApproveResponseDto;
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

		Payment payment = requestDto.toEntity();
		payment.assignTid(readyDto.tid());
		paymentRepository.save(payment);

		return new PaymentReadyResponseDto(readyDto.tid(), readyDto.nextRedirectPcUrl());
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

		return new PaymentApproveResponseDto(
			approveDto.tid(),
			LocalDateTime.parse(approveDto.approvedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
			BigDecimal.valueOf(approveDto.amount().total())
		);
	}
}