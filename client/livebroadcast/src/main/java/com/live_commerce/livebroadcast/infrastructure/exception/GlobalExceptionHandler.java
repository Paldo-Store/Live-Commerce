package com.live_commerce.livebroadcast.infrastructure.exception;


import com.live_commerce.livebroadcast.application.exception.CustomException;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {
		return ResponseUtil.customResponse(
			ex.getExceptionCode().getHttpStatus(),
			ex.getMessage()
		);
	}}
