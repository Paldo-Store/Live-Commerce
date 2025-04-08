package com.live_commerce.coupon.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponExceptionCode implements ExceptionCode {

  MISSING_MAX_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "정률 할인 유형에는 최대 주문 금액이 필수입니다."),
  MISSING_MIN_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "고정 할인 유형에는 최소 주문 금액이 필수입니다.");


  private final HttpStatus httpStatus;
  private final String message;

}