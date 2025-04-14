package com.live_commerce.chat.application.exception;


public class ChatException extends RuntimeException {

    private final String errorMessage;

    // 생성자: String 메시지 받기
    public ChatException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    // 메시지 반환
    public String getErrorMessage() {
        return errorMessage;
    }
}