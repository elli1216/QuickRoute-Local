## Repo: `mock-server` — Lightweight API Mocking Server

### Project state (critical)
- **Scaffold only.** Only `MockServerApplication.java` and its test exist. The architecture described in `README.md` (controllers, services, handlers, models, config, util) is aspirational — everything must be built.
- **Package is `com.elli.mockserver`**, *not* `com.mock.server` as the README tree shows. Place all source under `src/main/java/com/elli/mockserver/`.

### Stack
- Spring Boot 4.0.6, Java 26, Maven wrapper (`mvnw`).
- Dependencies: `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `spring-boot-devtools` (runtime).
- **No Lombok.** Write getters/setters/constructors manually or use records.
- No lint, formatter, or typecheck configuration.

### Commands
| Action | Command |
|--------|---------|
| Build | `./mvnw compile` |
| Test | `./mvnw test` (single test: `./mvnw test -Dtest=ClassName`) |
| Run | `./mvnw spring-boot:run` (listens on `:8080`) |

### Architecture intent (from README)
- Upload mock JSON definitions via `POST /mock/upload` → generates a `mockId`.
- Registered dynamically using `RequestMappingHandlerMapping` (see `DynamicRouteRegistrar` concept).
- Persisted to `mock-store/{mockId}.json` on disk; reloaded on startup.
- Catch-all handler serves `GET/POST/... /mock/{mockId}/**` with configurable delay, status, and path variable substitution.

### Implementation order (per README)
1. Model classes (`RouteDefinition`, etc.)
2. `MockRequestHandler` (simple echo first)
3. `DynamicRouteRegistrar` (test with hardcoded route)
4. `MockManagementController` (parse JSON, register)
5. `MockRegistryService` + `PersistenceService`

### Gotchas
- `mock-store/` directory is created at runtime by `PersistenceService` — add it to `.gitignore` if you commit.
- Spring Boot 4.x is very new; verify compatibility assumptions against the actual POM versions.
- The README uses Gradle commands (`./gradlew bootRun`) — this project is Maven-only.
