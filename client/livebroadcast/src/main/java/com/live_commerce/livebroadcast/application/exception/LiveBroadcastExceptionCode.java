package com.live_commerce.livebroadcast.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LiveBroadcastExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "LiveBroadcast Not Found"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
  ROLE_CHANGE_FORBIDDEN(HttpStatus.FORBIDDEN, "권한(Role)은 마스터만 수정할 수 있습니다."),
  BROADCAST_PRODUCT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "이미 방송과 연결된 상품입니다."),
  EXTERNAL_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");



  private final HttpStatus httpStatus;
  private final String message;

}