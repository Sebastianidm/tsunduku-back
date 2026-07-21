package com.tsundoku.backend.common.exception;

public class RateLimitExceededException extends ApiException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
