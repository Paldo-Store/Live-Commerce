package com.live_commerce.product.product.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
  EXTERNAL_COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "업체를 찾을 수 없습니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
  EXCEEDS_MAX_REQUEST_LIMIT(HttpStatus.BAD_REQUEST, "상품 ID는 최대 100개까지 요청할 수 있습니다.");


  private final HttpStatus httpStatus;
  private final String message;

}