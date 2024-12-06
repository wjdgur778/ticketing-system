package com.example.ticketing.common.exception;


public class RestApiException extends RuntimeException {
    private final CommonErrorCode errorCode;

    public RestApiException(CommonErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CommonErrorCode getErrorCode() {
        return errorCode;
    }
}