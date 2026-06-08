package com.elli.mockserver.exception;

public class MockNotFoundException extends RuntimeException {

    private final String mockId;

    public MockNotFoundException(String mockId) {
        super("Mock not found: " + mockId);
        this.mockId = mockId;
    }

    public String getMockId() {
        return mockId;
    }
}
