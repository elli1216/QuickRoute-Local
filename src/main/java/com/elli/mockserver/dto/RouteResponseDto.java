package com.elli.mockserver.dto;

public class RouteResponseDto {

    private String method;
    private String pathPattern;
    private int statusCode;
    private int delayMs;

    public RouteResponseDto() {
    }

    public RouteResponseDto(String method, String pathPattern, int statusCode, int delayMs) {
        this.method = method;
        this.pathPattern = pathPattern;
        this.statusCode = statusCode;
        this.delayMs = delayMs;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(int delayMs) {
        this.delayMs = delayMs;
    }
}
