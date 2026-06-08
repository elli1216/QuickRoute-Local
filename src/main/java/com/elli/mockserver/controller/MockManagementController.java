package com.elli.mockserver.controller;

import com.elli.mockserver.dto.MockSummaryDto;
import com.elli.mockserver.dto.MockUploadResponse;
import com.elli.mockserver.dto.RouteConfigDto;
import com.elli.mockserver.dto.RouteResponseDto;
import com.elli.mockserver.exception.MockNotFoundException;
import com.elli.mockserver.exception.MockUploadException;
import com.elli.mockserver.model.MockConfiguration;
import com.elli.mockserver.model.RouteDefinition;
import com.elli.mockserver.service.DynamicRouteRegistrar;
import com.elli.mockserver.service.MockRegistryService;
import com.elli.mockserver.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class MockManagementController {

    @Autowired
    private DynamicRouteRegistrar routeRegistrar;

    @Autowired
    private MockRegistryService registry;

    @Autowired
    private PersistenceService persistence;

    @PostMapping("/mock/upload")
    public ResponseEntity<MockUploadResponse> uploadMock(@RequestBody Map<String, RouteConfigDto> definition) {
        String mockId = UUID.randomUUID().toString();
        List<RouteDefinition> routes = parseDefinition(definition);

        routes.forEach(route -> routeRegistrar.registerRoute(mockId, route));
        registry.registerMock(mockId, routes);
        persistence.save(mockId, routes);

        return ResponseEntity.ok(new MockUploadResponse(mockId));
    }

    @GetMapping("/mocks")
    public ResponseEntity<Map<String, MockSummaryDto>> listMocks() {
        Map<String, MockSummaryDto> result = new LinkedHashMap<>();
        for (String id : registry.getMockIds()) {
            MockConfiguration config = registry.getMock(id);
            if (config == null)
                continue;
            result.put(id, new MockSummaryDto(
                    config.getId(),
                    config.getRoutes().size(),
                    config.getCreatedAt(),
                    config.getRoutes().stream()
                            .map(r -> new RouteResponseDto(r.getMethod(), r.getPathPattern(), r.getStatusCode(),
                                    r.getDelayMs()))
                            .toList()));
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/mock/{mockId}")
    public ResponseEntity<Void> deleteMock(@PathVariable String mockId) {
        List<RouteDefinition> routes = registry.getRoutes(mockId);
        if (routes.isEmpty()) {
            throw new MockNotFoundException(mockId);
        }
        routes.forEach(route -> routeRegistrar.unregisterRoute(mockId, route));
        registry.removeMock(mockId);
        persistence.delete(mockId);
        return ResponseEntity.noContent().build();
    }

    private List<RouteDefinition> parseDefinition(Map<String, RouteConfigDto> definition) {
        List<RouteDefinition> routes = new ArrayList<>();
        for (var entry : definition.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split(" ", 2);
            if (parts.length != 2) {
                throw new MockUploadException("Invalid route key: " + key + " — expected format: METHOD /path");
            }

            String method = parts[0].toUpperCase();
            String path = parts[1];
            RouteConfigDto config = entry.getValue();

            RouteDefinition route = new RouteDefinition();
            route.setMethod(method);
            route.setPathPattern(path);

            if (config.getStatus() != null) {
                route.setStatusCode(config.getStatus());
            }
            if (config.getDelay() != null) {
                route.setDelayMs(config.getDelay());
            }
            route.setResponseBody(config.getBody());

            routes.add(route);
        }
        return routes;
    }
}
