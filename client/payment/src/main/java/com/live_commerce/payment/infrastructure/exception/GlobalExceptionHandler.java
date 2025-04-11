package com.live_commerce.payment.infrastructure.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.infrastructure.common.ResponseUtil;
import com.live_commerce.payment.presentation.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {
		return ResponseUtil.customResponse(
			ex.getExceptionCode().getHttpStatus(),
			ex.getMessage()
		);
	}}
