package com.live_commerce.ai.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AiExceptionCode implements ExceptionCode {

  SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청 직렬화 중 오류가 발생했습니다.");


  private final HttpStatus httpStatus;
  private final String message;

}