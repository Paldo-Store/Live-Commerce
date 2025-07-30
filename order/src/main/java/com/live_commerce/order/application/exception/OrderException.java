package com.live_commerce.order.application.exception;

import org.springframework.http.HttpStatus;

public class OrderException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;

    public OrderException(OrderExceptionCode code) {
        super(code.getMessage());
        this.httpStatus = code.getHttpStatus();
        this.message = code.getMessage();
    }

    public OrderException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}