package com.elli.mockserver.exception;

public class RouteNotFoundException extends RuntimeException {

    private final String requestUri;
    private final String method;

    public RouteNotFoundException(String requestUri, String method) {
        super("Route not found: " + method + " " + requestUri);
        this.requestUri = requestUri;
        this.method = method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getMethod() {
        return method;
    }
}
