package com.example.service.exception;

public class CriticalException extends RuntimeException {

    private final String errorCode;

    public CriticalException(String message) {
        super(message);
        this.errorCode = "CRITICAL_ERROR";
    }

    public CriticalException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CriticalException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
