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
  UNAUTHORIZED(HttpStatus.FORBIDDEN, "해당 결제에 대한 접근 권한이 없습니다."),
  INVALID_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 상태입니다."),
  DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "이미 결제된 주문입니다."),
  DUPLICATE_PAYMENT_IN_PROGRESS(HttpStatus.CONFLICT, "해당 주문건은 현재 결제가 진행 중입니다."),
  PAYMENT_EXPIRED(HttpStatus.GONE, "결제 유효시간이 만료되었습니다."),
  PAYMENT_REFUND_FAIL(HttpStatus.BAD_GATEWAY, "결제 환불에 실패했습니다.");


  private final HttpStatus httpStatus;
  private final String message;

}