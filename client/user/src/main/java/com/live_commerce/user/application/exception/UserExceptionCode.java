package com.live_commerce.user.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found");


  private final HttpStatus httpStatus;
  private final String message;

}