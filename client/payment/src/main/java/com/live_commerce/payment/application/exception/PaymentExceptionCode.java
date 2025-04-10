package com.live_commerce.payment.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다."),
  PAYMENT_READY_FAIL(HttpStatus.BAD_GATEWAY, "결제 준비에 실패했습니다."),
  PAYMENT_APPROVE_FAIL(HttpStatus.BAD_GATEWAY, "결제 승인에 실패했습니다."),
  UNAUTHORIZED(HttpStatus.FORBIDDEN, "해당 결제에 대한 접근 권한이 없습니다.");


  private final HttpStatus httpStatus;
  private final String message;

}