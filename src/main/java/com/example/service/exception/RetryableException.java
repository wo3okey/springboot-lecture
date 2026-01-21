package com.example.service.exception;

public class RetryableException extends RuntimeException {

    private final int attemptCount;

    public RetryableException(String message) {
        super(message);
        this.attemptCount = 0;
    }

    public RetryableException(String message, int attemptCount) {
        super(message);
        this.attemptCount = attemptCount;
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
        this.attemptCount = 0;
    }

    public int getAttemptCount() {
        return attemptCount;
    }
}
