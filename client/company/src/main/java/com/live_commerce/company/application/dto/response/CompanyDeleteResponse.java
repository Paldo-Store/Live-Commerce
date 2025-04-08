package com.live_commerce.company.application.dto.response;

public class CompanyDeleteResponse {
    private final String message;

    public CompanyDeleteResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
