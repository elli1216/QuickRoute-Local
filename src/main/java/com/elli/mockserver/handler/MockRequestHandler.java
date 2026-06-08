package com.elli.mockserver.handler;

import com.elli.mockserver.exception.RouteNotFoundException;
import com.elli.mockserver.model.RouteDefinition;
import com.elli.mockserver.service.MockRegistryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MockRequestHandler {

    @Autowired
    private MockRegistryService registry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        RouteDefinition route = registry.findRoute(requestUri, method);
        if (route == null) {
            throw new RouteNotFoundException(requestUri, method);
        }

        if (route.getDelayMs() > 0) {
            Thread.sleep(route.getDelayMs());
        }

        Map<String, String> pathVars = registry.extractPathVariables(requestUri, route.getPathPattern());
        Object finalBody = substitutePathVars(route.getResponseBody(), pathVars);

        response.setStatus(route.getStatusCode());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), finalBody);
    }

    private Object substitutePathVars(Object body, Map<String, String> pathVars) {
        if (body instanceof String text && pathVars != null) {
            for (var entry : pathVars.entrySet()) {
                text = text.replace(":" + entry.getKey(), entry.getValue());
            }
            return text;
        }
        if (body instanceof Map<?, ?> map) {
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            for (var entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                if (pathVars != null && pathVars.containsKey(key)) {
                    value = pathVars.get(key);
                } else {
                    value = substitutePathVars(value, pathVars);
                }
                result.put(key, value);
            }
            return result;
        }
        if (body instanceof java.util.List<?> list) {
            return list.stream()
                    .map(item -> substitutePathVars(item, pathVars))
                    .toList();
        }
        return body;
    }
}
