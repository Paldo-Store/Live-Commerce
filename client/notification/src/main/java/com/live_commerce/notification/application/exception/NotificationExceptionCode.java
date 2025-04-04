package com.live_commerce.notification.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "Notification Not Found");


  private final HttpStatus httpStatus;
  private final String message;

}