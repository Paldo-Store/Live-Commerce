package com.live_commerce.livebroadcast.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LiveBroadcastExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "방송을 찾을 수 없습니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
  ROLE_CHANGE_FORBIDDEN(HttpStatus.FORBIDDEN, "권한(Role)은 마스터만 수정할 수 있습니다."),
  BROADCAST_PRODUCT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "이미 방송과 연결된 상품입니다."),
  EXTERNAL_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
  PRODUCT_DISCONNECTED(HttpStatus.NOT_FOUND, "해당 방송과 연결된 상품이 없습니다."),
  EXTERNAL_COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "업체를 찾을 수 없습니다."),
  EXCEPTION_FIELD_REQUIRED(HttpStatus.BAD_REQUEST, "수정할 항목이 없습니다."),
  INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "시작 시간은 종료 시간보다 빨라야 합니다.");




  private final HttpStatus httpStatus;
  private final String message;

}