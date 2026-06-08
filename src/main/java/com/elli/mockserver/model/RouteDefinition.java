package com.elli.mockserver.model;

public class RouteDefinition {

    private String method;
    private String pathPattern;
    private Object responseBody;
    private int delayMs;
    private int statusCode;

    public RouteDefinition() {
        this.delayMs = 0;
        this.statusCode = 200;
    }

    public RouteDefinition(String method, String pathPattern, Object responseBody, int delayMs, int statusCode) {
        this.method = method;
        this.pathPattern = pathPattern;
        this.responseBody = responseBody;
        this.delayMs = delayMs;
        this.statusCode = statusCode;
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

    public Object getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(int delayMs) {
        this.delayMs = delayMs;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
