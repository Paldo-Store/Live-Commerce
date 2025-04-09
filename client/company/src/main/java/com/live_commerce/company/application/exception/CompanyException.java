package com.live_commerce.company.application.exception;

public class CompanyException extends RuntimeException {

    private final CompanyExceptionCode errorCode;

    public CompanyException(CompanyExceptionCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CompanyExceptionCode getErrorCode() {
        return errorCode;
    }
}