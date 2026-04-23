package com.live_commerce.livebroadcast.infrastructure.exception;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.live_commerce.livebroadcast.application.exception.CustomException;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {

		return ResponseUtil.customResponse(
			ex.getExceptionCode().getHttpStatus(),
			ex.getMessage()
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleUnexpectedException(Exception ex) {
		ex.printStackTrace(); // 로그 확인용
		return ResponseUtil.customResponse(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"서버 내부 오류가 발생했습니다."
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

		String paramName = ex.getName();
		Object invalidValue = ex.getValue();

		String message = String.format("잘못된 형식의 %s입니다. 입력값: %s", paramName, invalidValue);

		return ResponseUtil.customResponse(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<String>> handleInvalidJson(HttpMessageNotReadableException ex) {
		Throwable cause = ex.getCause();

		if (cause instanceof InvalidFormatException ife) {
			if (ife.getTargetType().isEnum()) {
				return ResponseUtil.customResponse(
						HttpStatus.BAD_REQUEST,
						String.format(
								"'%s'은(는) 올바르지 않은 상태값입니다",
								ife.getValue()
						)
				);
			}
		}

		if (cause instanceof InvalidFormatException ife && ife.getTargetType().equals(LocalDateTime.class)) {
			return ResponseUtil.customResponse(
					HttpStatus.BAD_REQUEST,
					"날짜 형식이 잘못되었습니다. yyyy-MM-dd'T'HH:mm:ss 형식으로 입력해주세요."
			);
		}

		return ResponseUtil.customResponse(
				HttpStatus.BAD_REQUEST,
				"잘못된 요청입니다. 요청 형식을 다시 확인해주세요."
		);
	}

}
