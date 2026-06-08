package com.elli.mockserver.model;

import java.time.LocalDateTime;
import java.util.List;

public class MockConfiguration {

    private String id;
    private List<RouteDefinition> routes;
    private LocalDateTime createdAt;

    public MockConfiguration() {
    }

    public MockConfiguration(String id, List<RouteDefinition> routes) {
        this.id = id;
        this.routes = routes;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
