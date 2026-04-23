package com.live_commerce.company.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "Company Not Found");


  private final HttpStatus httpStatus;
  private final String message;

}