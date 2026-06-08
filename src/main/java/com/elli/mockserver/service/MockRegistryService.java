package com.elli.mockserver.service;

import com.elli.mockserver.model.MockConfiguration;
import com.elli.mockserver.model.RouteDefinition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MockRegistryService {

    private final Map<String, MockConfiguration> mocks = new ConcurrentHashMap<>();

    public void registerMock(String mockId, List<RouteDefinition> routes) {
        mocks.put(mockId, new MockConfiguration(mockId, routes));
    }

    public void removeMock(String mockId) {
        mocks.remove(mockId);
    }

    public List<RouteDefinition> getRoutes(String mockId) {
        MockConfiguration config = mocks.get(mockId);
        return config != null ? config.getRoutes() : List.of();
    }

    public RouteDefinition findRoute(String requestUri, String method) {
        String mockId = extractMockId(requestUri);
        if (mockId == null) return null;

        MockConfiguration config = mocks.get(mockId);
        if (config == null) return null;

        String relativePath = extractRelativePath(requestUri, mockId);

        for (RouteDefinition route : config.getRoutes()) {
            if (route.getMethod().equalsIgnoreCase(method) && matchesPath(route.getPathPattern(), relativePath)) {
                return route;
            }
        }
        return null;
    }

    public Map<String, String> extractPathVariables(String requestUri, String pathPattern) {
        Map<String, String> vars = new LinkedHashMap<>();
        String mockId = extractMockId(requestUri);
        if (mockId == null) return vars;

        String relativePath = extractRelativePath(requestUri, mockId);
        Pattern pattern = buildPattern(pathPattern);
        Matcher matcher = pattern.matcher(relativePath);

        if (matcher.matches()) {
            List<String> varNames = extractVarNames(pathPattern);
            for (int i = 0; i < varNames.size(); i++) {
                vars.put(varNames.get(i), matcher.group(i + 1));
            }
        }
        return vars;
    }

    public MockConfiguration getMock(String mockId) {
        return mocks.get(mockId);
    }

    public Set<String> getMockIds() {
        return mocks.keySet();
    }

    private String extractMockId(String uri) {
        String[] parts = uri.replaceFirst("^/", "").split("/");
        if (parts.length > 1 && "mock".equals(parts[0])) {
            return parts[1];
        }
        return null;
    }

    private String extractRelativePath(String uri, String mockId) {
        String prefix = "/mock/" + mockId;
        String relative = uri.substring(prefix.length());
        return relative.isEmpty() ? "/" : relative;
    }

    private boolean matchesPath(String pattern, String requestPath) {
        return buildPattern(pattern).matcher(requestPath).matches();
    }

    private Pattern buildPattern(String pathPattern) {
        String regex = Arrays.stream(pathPattern.split("/"))
                .map(segment -> segment.startsWith(":") ? "([^/]+)" : Pattern.quote(segment))
                .reduce((a, b) -> a + "/" + b)
                .orElse("");
        return Pattern.compile("^" + regex + "$");
    }

    private List<String> extractVarNames(String pathPattern) {
        return Arrays.stream(pathPattern.split("/"))
                .filter(segment -> segment.startsWith(":"))
                .map(segment -> segment.substring(1))
                .toList();
    }
}
