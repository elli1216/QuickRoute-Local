# Lightweight API Mocking Server

A Spring Boot application that lets you define and register mock API endpoints at runtime through a simple JSON upload. Designed to be simple, extensible, and ready for incremental feature addition.

---

## Architecture

```
┌─────────────────┐     ┌─────────────────────────────────────────────────┐
│   Client        │     │              Mock Server (Spring Boot)           │
│ (Postman/UI)    │────▶│                                                 │
└─────────────────┘     │  ┌─────────────┐    ┌────────────────────────┐ │
                        │  │ Upload API  │───▶│ Mock Configuration      │ │
                        │  │ /mock/upload│    │ (JSON → RouteDefinition)│ │
                        │  └─────────────┘    └───────────┬────────────┘ │
                        │                                 │              │
                        │                                 ▼              │
                        │  ┌─────────────┐    ┌────────────────────────┐ │
                        │  │ Dynamic     │◀───│ In‑memory Registry      │ │
                        │  │ Route       │    │ (MockId → Routes)       │ │
                        │  │ Registrar   │────│ + Persistence (file)    │ │
                        │  └─────────────┘    └───────────┬────────────┘ │
                        │                                 │              │
                        │                                 ▼              │
                        │  ┌──────────────────────────────────────────┐  │
                        │  │   Request Handling Dispatcher            │  │
                        │  │   (Forward to registered mock handler)   │  │
                        │  └──────────────────────────────────────────┘  │
                        └─────────────────────────────────────────────────┘
```

---

## Project Structure

```
src/main/java/com/mock/server/
├── MockServerApplication.java          # @SpringBootApplication
├── controller/
│   ├── MockManagementController.java   # POST /mock/upload, GET /mocks, DELETE /mock/{id}
│   └── MockRequestDispatcher.java      # Handles all dynamic requests (catch‑all)
├── service/
│   ├── MockRegistryService.java        # Registry of mock configurations
│   ├── DynamicRouteRegistrar.java      # Adds/removes routes at runtime
│   └── PersistenceService.java         # Saves/loads mocks to disk
├── model/
│   ├── MockConfiguration.java          # Root object: id, routes, created
│   ├── RouteDefinition.java            # path, method, responseBody, delay, status
│   └── MockRequest.java                # For logging/dispatch
├── handler/
│   └── MockRequestHandler.java         # Actual logic to serve a mocked endpoint
├── config/
│   └── WebConfig.java                  # CORS, async config if needed
└── util/
    └── PathVariableExtractor.java      # Parses :id from path
```

---

## Key Components

### RouteDefinition Model
Maps a single mock route: HTTP method, path pattern (`/users/:id`), response body, optional delay, and status code.

### DynamicRouteRegistrar
Uses Spring's `RequestMappingHandlerMapping` to register and unregister new handler methods at runtime without restarting the server.

### MockRequestHandler
The core handler that receives all mocked requests, looks up the matching route definition, applies simulated delays, substitutes path variables, and returns the configured JSON response.

### MockRegistryService
An in-memory `ConcurrentHashMap` that stores mock configurations keyed by mock ID. Provides pattern matching against request paths.

### MockManagementController
Exposes REST endpoints to upload new mock definitions, list all registered mocks, and delete existing ones.

### PersistenceService
Saves mock configurations as JSON files to disk (`mock-store/`). On startup, reloads all saved mocks and re-registers their routes automatically.

---

## Execution Flow

1. **Upload** – Client sends a JSON mock definition to `POST /mock/upload`.  
2. **Parse** – The definition is converted into a list of `RouteDefinition` objects.  
3. **Register** – For each route, `DynamicRouteRegistrar.registerRoute()` adds a new mapping to Spring's `HandlerMapping`.  
4. **Store** – The mock is saved in memory (`MockRegistryService`) and persisted to disk (`mock-store/{mockId}.json`).  
5. **Request** – A client calls the mocked endpoint (e.g., `/mock/abc123/users/5`).  
6. **Dispatch** – The request is dispatched to `MockRequestHandler.handle()`.  
7. **Response** – The handler looks up the route, applies any configured delay, substitutes path variables, and returns the JSON response.

---

## Getting Started

### Prerequisites
- Java 17+
- Maven or Gradle
- Spring Boot 3.x

### Dependencies
- Spring Web
- Spring Boot DevTools (optional)
- Jackson (included with Spring Boot)

### Build & Run
```bash
./mvnw spring-boot:run
```
or
```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

---

## API Endpoints

| Method | Endpoint             | Description                  |
|--------|----------------------|------------------------------|
| POST   | `/mock/upload`       | Upload a new mock definition |
| GET    | `/mocks`             | List all registered mocks    |
| DELETE | `/mock/{mockId}`     | Delete a mock and its routes |
| ANY    | `/mock/{mockId}/**`  | Mocked endpoint (dynamic)    |

### Upload Example
```json
{
  "GET /users": { "status": 200, "body": [{ "id": 1, "name": "Alice" }] },
  "GET /users/:id": { "status": 200, "body": { "id": ":id", "name": "User :id" } },
  "POST /users": { "status": 201, "body": { "id": 99, "name": "Created" }, "delay": 500 }
}
```

Response:
```json
{ "mockId": "a1b2c3d4-e5f6-..." }
```

You can then call:
```
GET http://localhost:8080/mock/a1b2c3d4-e5f6-.../users
GET http://localhost:8080/mock/a1b2c3d4-e5f6-.../users/42
POST http://localhost:8080/mock/a1b2c3d4-e5f6-.../users
```

---

## Startup & Shutdown

- **Startup** – `PersistenceService` loads all saved mock configurations from `mock-store/` and re-registers their routes dynamically.  
- **Shutdown** – No explicit action needed; configurations persist on disk and are reloaded on the next startup.
