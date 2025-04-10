package com.live_commerce.inventory.application.exception;


import com.live_commerce.product.application.exception.ExceptionCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final com.live_commerce.product.application.exception.ExceptionCode exceptionCode;

  public CustomException(ExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
  }
}