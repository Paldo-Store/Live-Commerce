package com.live_commerce.ai.infrastructure.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.live_commerce.ai.application.exception.CustomException;
import com.live_commerce.ai.infrastructure.common.ResponseUtil;
import com.live_commerce.ai.presentation.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {
		return ResponseUtil.customResponse(
			ex.getExceptionCode().getHttpStatus(),
			ex.getMessage()
		);
	}}
