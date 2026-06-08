package com.elli.mockserver.exception;

public class MockUploadException extends RuntimeException {

    public MockUploadException(String message) {
        super(message);
    }

    public MockUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
