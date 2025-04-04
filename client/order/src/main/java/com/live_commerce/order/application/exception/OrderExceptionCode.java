package com.live_commerce.order.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "Order Not Found");


  private final HttpStatus httpStatus;
  private final String message;

}