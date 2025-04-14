package com.live_commerce.product.inventory.application.exception;


import org.springframework.http.HttpStatus;

public interface ExceptionCode {

  HttpStatus getHttpStatus();
  String getMessage();
}