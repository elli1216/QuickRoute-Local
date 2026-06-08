package com.elli.mockserver.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MockSummaryDto {

    private String mockId;
    private int routeCount;
    private LocalDateTime createdAt;
    private List<RouteResponseDto> routes;

    public MockSummaryDto() {
    }

    public MockSummaryDto(String mockId, int routeCount, LocalDateTime createdAt, List<RouteResponseDto> routes) {
        this.mockId = mockId;
        this.routeCount = routeCount;
        this.createdAt = createdAt;
        this.routes = routes;
    }

    public String getMockId() {
        return mockId;
    }

    public void setMockId(String mockId) {
        this.mockId = mockId;
    }

    public int getRouteCount() {
        return routeCount;
    }

    public void setRouteCount(int routeCount) {
        this.routeCount = routeCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<RouteResponseDto> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteResponseDto> routes) {
        this.routes = routes;
    }
}
