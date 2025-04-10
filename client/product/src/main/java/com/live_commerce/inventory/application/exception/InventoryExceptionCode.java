package com.live_commerce.inventory.application.exception;


import com.live_commerce.product.application.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found");


  private final HttpStatus httpStatus;
  private final String message;

}