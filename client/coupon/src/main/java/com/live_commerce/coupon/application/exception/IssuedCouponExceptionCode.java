package com.live_commerce.coupon.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IssuedCouponExceptionCode implements ExceptionCode {

  COUPON_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "제공된 쿠폰 코드에 대한 쿠폰 정책을 찾을 수 없습니다."),
  COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "이미 사용자에게 쿠폰이 발곱되었습니다.");

  private final HttpStatus httpStatus;
  private final String message;

}