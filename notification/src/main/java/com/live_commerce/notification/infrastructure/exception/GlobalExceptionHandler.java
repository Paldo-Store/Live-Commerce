package com.live_commerce.notification.infrastructure.exception;

import com.live_commerce.notification.presentation.common.ApiResponse;
import javax.naming.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("인증이 필요합니다.", null));
  }
}
