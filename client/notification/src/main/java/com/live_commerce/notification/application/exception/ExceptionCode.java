package com.live_commerce.notification.application.exception;


import org.springframework.http.HttpStatus;

public interface ExceptionCode {

  HttpStatus getHttpStatus();
  String getMessage();
}