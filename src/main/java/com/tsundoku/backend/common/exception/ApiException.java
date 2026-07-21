package com.tsundoku.backend.common.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
