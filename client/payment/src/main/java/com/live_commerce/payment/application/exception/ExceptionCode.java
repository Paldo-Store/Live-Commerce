package com.live_commerce.payment.application.exception;


import org.springframework.http.HttpStatus;

public interface ExceptionCode {

  HttpStatus getHttpStatus();
  String getMessage();
}