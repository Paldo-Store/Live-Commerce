package com.live_commerce.livebroadcast.application.exception;


import org.springframework.http.HttpStatus;

public interface ExceptionCode {

  HttpStatus getHttpStatus();
  String getMessage();
}