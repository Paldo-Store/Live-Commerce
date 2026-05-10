package com.live_commerce.ai.infrastructure.common;

import com.live_commerce.ai.presentation.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

  public static <T> ResponseEntity<ApiResponse<T>> success(T body) {
    return ResponseEntity.ok(buildResponse(body));
  }

  public static ResponseEntity<ApiResponse<Void>> noContent() {
    return ResponseEntity.noContent().build();
  }

  public static <T> ResponseEntity<ApiResponse<T>> customResponse(HttpStatus status, T body) {
    ApiResponse<T> response = new ApiResponse<>(status.is2xxSuccessful() ? "success" : "error", body);
    return ResponseEntity.status(status).body(response);
  }

  private static <T> ApiResponse<T> buildResponse(T body) {
    return new ApiResponse<>("success", body);
  }
}
