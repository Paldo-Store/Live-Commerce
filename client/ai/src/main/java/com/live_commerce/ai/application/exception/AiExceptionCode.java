package com.live_commerce.ai.application.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AiExceptionCode implements ExceptionCode {

  SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청 직렬화 중 오류가 발생했습니다."),
  ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 분석 결과를 찾을 수 없습니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다."),
  GEMINI_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "AI 분석 처리 중 오류가 발생했습니다.");



  private final HttpStatus httpStatus;
  private final String message;

}