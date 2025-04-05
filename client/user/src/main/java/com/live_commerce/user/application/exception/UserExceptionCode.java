package com.live_commerce.user.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserExceptionCode implements ExceptionCode {

  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 존재하는 사용자 ID입니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");

  private final HttpStatus httpStatus;
  private final String message;

}