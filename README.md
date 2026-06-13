# QuickRoute-Local - Lightweight API Mocking Server

Develop and test against realistic API responses **before your real backend exists** — no coding, no restarts, no external dependencies.

Upload a JSON mock definition once and instantly get live HTTP endpoints with the exact status codes, response bodies, and delays you need. Add, remove, or change routes at any point while the server is running: no redeploy, no downtime.

**Why this matters:**
- **Unblock frontend development** — build and test UI flows against real HTTP calls, not fake data in your app code.
- **Simulate real-world conditions** — configure response delays to test loading states, timeouts, and race conditions.
- **Stay in control** — return any HTTP status (200, 201, 404, 500, …) to exercise every branch of your error handling.
- **No extra infrastructure** — just JSON over HTTP. No database, no container orchestration, no external service to provision.
- **Survives restarts** — mocks persist to disk and reload automatically when the server starts.

Designed to be simple, gets out of your way, and easy to extend if you need more.

---

## Architecture

```
┌─────────────────┐     ┌─────────────────────────────────────────────────┐
│   Client        │     │              Mock Server (Spring Boot)          │
│ (Postman/UI)    │────>│                                                 │
└─────────────────┘     │  ┌─────────────┐    ┌─────────────────────────┐ │
                        │  │ Upload API  │───>│ Mock Configuration      │ │
                        │  │ /mock/upload│    │ (JSON → RouteDefinition)│ │
                        │  └─────────────┘    └───────────┬─────────────┘ │
                        │                                 │               │
                        │                                 ▼               │
                        │  ┌─────────────┐    ┌────────────────────────┐  │
                        │  │ Dynamic     │<───│ In‑memory Registry     │  │
                        │  │ Route       │    │ (MockId → Routes)      │  │
                        │  │ Registrar   │────│ + Persistence (file)   │  │
                        │  └─────────────┘    └───────────┬────────────┘  │
                        │                                 │               │
                        │                                 ▼               │
                        │  ┌──────────────────────────────────────────┐   │
                        │  │   Request Handling Dispatcher            │   │
                        │  │   (Forward to registered mock handler)   │   │
                        │  └──────────────────────────────────────────┘   │
                        └─────────────────────────────────────────────────┘
```

---

## Browser UI

When the server is running, open `http://localhost:8080` in your browser to access the built-in management UI. No extra tools needed.

The UI lets you:
- Paste or write mock JSON in a textarea with **Format** and **Sample** buttons
- Upload mocks and copy the returned `mockId`
- Browse all registered mocks with their routes, status codes, delays, and full endpoint URLs
- Copy any endpoint URL to the clipboard
- Delete mocks with one click

> **Tip:** You can also use any HTTP client (cURL, Postman, Bruno) to call the same REST API described below.

---

## Project Structure

```
src/main/java/com/elli/mockserver/
├── MockServerApplication.java          # @SpringBootApplication
├── controller/
│   └── MockManagementController.java   # POST /mock/upload, GET /mocks, DELETE /mock/{id}
├── service/
│   ├── MockRegistryService.java        # Registry of mock configurations
│   ├── DynamicRouteRegistrar.java      # Adds/removes routes at runtime
│   └── PersistenceService.java         # Saves/loads mocks to disk
├── model/
│   ├── MockConfiguration.java          # Root object: id, routes, created
│   └── RouteDefinition.java            # path, method, responseBody, delay, status
├── handler/
│   └── MockRequestHandler.java         # Serves a mocked endpoint
├── dto/
│   ├── RouteConfigDto.java, MockUploadResponse.java, RouteResponseDto.java
│   ├── MockSummaryDto.java, ErrorResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java     # Catches & formats all errors as JSON
│   └── MockNotFoundException.java, MockUploadException.java,
│       RouteNotFoundException.java, PersistenceException.java,
│       RouteRegistrationException.java
└── resources/static/
    ├── index.html                      # Browser UI
    └── style.css                       # UI styles
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
- Java 26+
- Maven (wrapped via `mvnw`)

### Build & Run
```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

---

## API Endpoints

| Method | Endpoint             | Description                  |
|--------|----------------------|------------------------------|
| GET    | `/`                  | Browser UI (index.html)      |
| POST   | `/mock/upload`       | Upload a new mock definition |
| GET    | `/mocks`             | List all registered mocks    |
| DELETE | `/mock/{mockId}`     | Delete a mock and its routes |
| ANY    | `/mock/{mockId}/**`  | Mocked endpoint (dynamic)    |

---

## Tutorial

### POST /mock/upload — Create a new mock

Upload a JSON object where each key is `"METHOD /path"` and each value is a route configuration.

**Request body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `"METHOD /path"` key | `string` | yes | HTTP method and path, e.g. `"GET /users/:id"`. Path segments starting with `:` are treated as variables. |
| `status` | `integer` | no | HTTP status code. Default: `200`. |
| `delay` | `integer` | no | Response delay in milliseconds. Default: `0`. |
| `body` | any JSON | no | The response body. Can be an object, array, string, number, or `null`. |

**Example request:**
```json
{
  "GET /users": {
    "status": 200,
    "body": [{ "id": 1, "name": "Alice" }, { "id": 2, "name": "Bob" }]
  },
  "GET /users/:id": {
    "status": 200,
    "body": { "id": ":id", "name": "User :id" }
  },
  "POST /users": {
    "status": 201,
    "body": { "id": 99, "name": "Created" },
    "delay": 500
  }
}
```

**Success response (201):**
```json
{
  "mockId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"
}
```

**Error responses:**
```json
// 400 — Invalid route key format
{ "error": "Invalid route key: badkey — expected format: METHOD /path", "timestamp": "2026-06-08T22:00:00Z" }

// 400 — Missing or invalid fields
{ "error": "Invalid status for GET /users: must be an integer", "timestamp": "2026-06-08T22:00:00Z" }
```

---

### GET /mocks — List all registered mocks

Returns every registered mock with its routes (without response bodies).

**Success response (200):**
```json
{
  "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d": {
    "mockId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
    "routeCount": 3,
    "createdAt": "2026-06-08T22:00:00",
    "routes": [
      { "method": "GET", "pathPattern": "/users", "statusCode": 200, "delayMs": 0 },
      { "method": "GET", "pathPattern": "/users/:id", "statusCode": 200, "delayMs": 0 },
      { "method": "POST", "pathPattern": "/users", "statusCode": 201, "delayMs": 500 }
    ]
  }
}
// Empty list:
{}
```

---

### DELETE /mock/{mockId} — Delete a mock

Removes all registered routes and the persisted file for the given mock ID.

**Success response (204):** No body.

**Error response (404):**
```json
{ "error": "Mock not found: unknown-id", "timestamp": "2026-06-08T22:00:00Z" }
```

---

### ANY /mock/{mockId}/** — Call a mocked endpoint

After uploading, call any registered route under the mock's base path. Path variables (`:id`, `:name`, etc.) are captured from the URL and substituted into `:variable` placeholders in the response body.

**Example calls using the mock above:**
```bash
# Returns the users list
curl http://localhost:8080/mock/a1b2c3d4-e5f6-.../users

# Returns a single user with :id substituted
curl http://localhost:8080/mock/a1b2c3d4-e5f6-.../users/42

# Creates a user — waits 500ms then returns 201
curl -X POST http://localhost:8080/mock/a1b2c3d4-e5f6-.../users
```

**Responses:**
```json
// GET /users → 200
[{ "id": 1, "name": "Alice" }, { "id": 2, "name": "Bob" }]

// GET /users/42 → 200  (:id variable substituted into response body)
{ "id": "42", "name": "User 42" }

// POST /users → 201  (after 500ms delay)
{ "id": 99, "name": "Created" }
```

**Error response (404) — no matching route:**
```json
{ "error": "Route not found: GET /mock/a1b2c3d4-e5f6-.../nonexistent", "timestamp": "2026-06-08T22:00:00Z" }
```

---

## Startup & Shutdown

- **Startup** – `PersistenceService` loads all saved mock configurations from `mock-store/` and re-registers their routes dynamically.  
- **Shutdown** – No explicit action needed; configurations persist on disk and are reloaded on the next startup.
