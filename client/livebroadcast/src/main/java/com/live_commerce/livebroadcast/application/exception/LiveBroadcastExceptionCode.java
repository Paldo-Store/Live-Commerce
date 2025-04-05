package com.live_commerce.livebroadcast.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LiveBroadcastExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "LiveBroadcast Not Found");


  private final HttpStatus httpStatus;
  private final String message;

}