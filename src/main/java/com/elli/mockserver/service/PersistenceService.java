package com.elli.mockserver.service;

import com.elli.mockserver.exception.PersistenceException;
import com.elli.mockserver.model.RouteDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class PersistenceService {

    private static final Path STORAGE_DIR = Paths.get("mock-store");

    @Autowired
    private DynamicRouteRegistrar routeRegistrar;

    @Autowired
    private MockRegistryService registry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(STORAGE_DIR);
        loadAllMocksOnStartup();
    }

    public void save(String mockId, List<RouteDefinition> routes) {
        try {
            Path file = STORAGE_DIR.resolve(mockId + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), routes);
        } catch (IOException e) {
            throw new PersistenceException("Failed to persist mock " + mockId, e);
        }
    }

    public void delete(String mockId) {
        try {
            Files.deleteIfExists(STORAGE_DIR.resolve(mockId + ".json"));
        } catch (IOException e) {
            throw new PersistenceException("Failed to delete persisted mock " + mockId, e);
        }
    }

    private void loadAllMocksOnStartup() {
        try (var files = Files.list(STORAGE_DIR)) {
            files.filter(p -> p.toString().endsWith(".json")).forEach(this::loadMock);
        } catch (IOException e) {
            // directory may be empty
        }
    }

    private void loadMock(Path file) {
        try {
            String mockId = file.getFileName().toString().replace(".json", "");
            List<RouteDefinition> routes = objectMapper.readValue(
                    file.toFile(), new TypeReference<List<RouteDefinition>>() {});
            registry.registerMock(mockId, routes);
            routes.forEach(route -> routeRegistrar.registerRoute(mockId, route));
        } catch (IOException e) {
            throw new PersistenceException("Failed to load mock from " + file, e);
        }
    }
}
