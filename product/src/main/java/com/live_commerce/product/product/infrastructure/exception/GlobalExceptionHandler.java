package com.live_commerce.product.product.infrastructure.exception;

import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.product.application.exception.CustomException;
import com.live_commerce.product.product.infrastructure.common.ResponseUtil;
import com.live_commerce.product.product.presentation.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {

        return ResponseUtil.customResponse(
                ex.getExceptionCode().getHttpStatus(),
                ex.getMessage()
        );
    }
}

