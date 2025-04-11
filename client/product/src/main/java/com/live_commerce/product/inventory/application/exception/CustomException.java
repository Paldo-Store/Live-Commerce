package com.live_commerce.product.inventory.application.exception;


import com.live_commerce.product.product.application.exception.ExceptionCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ExceptionCode exceptionCode;

  public CustomException(ExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
  }
}