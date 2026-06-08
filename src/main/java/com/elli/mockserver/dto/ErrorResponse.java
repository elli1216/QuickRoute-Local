package com.elli.mockserver.dto;

import java.time.Instant;

public class ErrorResponse {

    private String error;
    private String timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = Instant.now().toString();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
