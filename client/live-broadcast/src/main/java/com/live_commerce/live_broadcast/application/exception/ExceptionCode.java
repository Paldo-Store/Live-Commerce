package com.live_commerce.live_broadcast.application.exception;


import org.springframework.http.HttpStatus;

public interface ExceptionCode {

  HttpStatus getHttpStatus();
  String getMessage();
}