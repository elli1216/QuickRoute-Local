package com.elli.mockserver.dto;

public class MockUploadResponse {

    private String mockId;

    public MockUploadResponse() {
    }

    public MockUploadResponse(String mockId) {
        this.mockId = mockId;
    }

    public String getMockId() {
        return mockId;
    }

    public void setMockId(String mockId) {
        this.mockId = mockId;
    }
}
