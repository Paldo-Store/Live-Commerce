package com.live_commerce.payment.infrastructure.common;

import com.live_commerce.ai.presentation.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ResponseUtil {

  public static <T> ResponseEntity<ApiResponse<T>> success(T body) {
    ApiResponse<T> response = buildResponse(body);
    logResponse(response);
    return ResponseEntity.ok(response);
  }

  public static ResponseEntity<ApiResponse<Void>> noContent() {
    ApiResponse<Void> response = buildResponse(null);
    logResponse(response);
    return ResponseEntity.noContent().build();
  }

  public static ResponseEntity<ApiResponse<String>> notFound(String message) {
    log.error("Not found error: {}", message);
    return buildErrorResponse(message, HttpStatus.NOT_FOUND);
  }

  public static ResponseEntity<ApiResponse<String>> badRequest(String message) {
    log.error("Bad request error: {}", message);
    return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
  }

  public static <T> ResponseEntity<ApiResponse<T>> customResponse(HttpStatus status, T body) {
    log.info("Custom response: {}", body);
    ApiResponse<T> response = new ApiResponse<>(status.is2xxSuccessful() ? "success" : "error",
        body);
    return ResponseEntity.status(status).body(response);
  }

  public static ResponseEntity<ApiResponse<String>> internalServerError(String message) {
    log.error("Internal server error: {}", message);
    return buildErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static <T> ApiResponse<T> buildResponse(T body) {
    return new ApiResponse<>("success", body);
  }

  private static ResponseEntity<ApiResponse<String>> buildErrorResponse(String message,
      HttpStatus status) {
    ApiResponse<String> response = new ApiResponse<>("error", message);
    return ResponseEntity.status(status).body(response);
  }

  private static <T> void logResponse(ApiResponse<T> response) {
    log.info("Response: {}", response);
  }
}
