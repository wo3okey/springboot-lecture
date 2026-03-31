package com.example.service.exception;

import com.example.domain.enums.ReleaseFailureReason;

public class ReleaseProcessException extends RuntimeException {

    private final ReleaseFailureReason reason;

    public ReleaseProcessException(String message, ReleaseFailureReason reason) {
        super(message);
        this.reason = reason;
    }

    public ReleaseProcessException(String message, ReleaseFailureReason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public ReleaseFailureReason getReason() {
        return reason;
    }
}
