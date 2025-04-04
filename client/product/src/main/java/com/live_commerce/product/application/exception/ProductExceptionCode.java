package com.live_commerce.product.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found");


  private final HttpStatus httpStatus;
  private final String message;

}