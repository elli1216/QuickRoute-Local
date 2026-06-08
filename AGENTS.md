## Repo: `mock-server` — Lightweight API Mocking Server

### Project state
- All core components implemented: `model/`, `handler/`, `service/`, `controller/` packages exist.
- Package is `com.elli.mockserver`, **not** `com.mock.server` as the README tree shows.

### Stack
- Spring Boot 4.0.6, Java 26, Maven wrapper (`mvnw`).
- Dependencies: `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `spring-boot-devtools` (runtime), `jackson-databind`.
- **No Lombok.** Write getters/setters manually. No JPA — persistence is file-based (`mock-store/`).
- No lint, formatter, or typecheck configuration.

### Commands
| Action | Command |
|--------|---------|
| Build | `./mvnw compile` |
| Test | `./mvnw test` (single test: `./mvnw test -Dtest=ClassName`) |
| Run | `./mvnw spring-boot:run` (listens on `:8080`) |

### Key classes
| Class | Role |
|-------|------|
| `MockManagementController` | `POST /mock/upload`, `GET /mocks`, `DELETE /mock/{mockId}` |
| `DynamicRouteRegistrar` | Registers/unregisters routes in Spring via `RequestMappingHandlerMapping` at runtime |
| `MockRequestHandler` | Serves mocked responses: delay, path variable substitution in response body |
| `MockRegistryService` | In-memory `ConcurrentHashMap<String, MockConfiguration>` with pattern matching |
| `PersistenceService` | Saves/loads `mock-store/{mockId}.json`; reloads all on `@PostConstruct` |

### Gotchas
- **`@Qualifier("requestMappingHandlerMapping")`** is required on the `RequestMappingHandlerMapping` field in `DynamicRouteRegistrar` — Actuator creates a second bean (`controllerEndpointHandlerMapping`) that causes `NoUniqueBeanDefinitionException`.
- `mock-store/` directory is created at runtime by `PersistenceService` — add to `.gitignore`.
- Jackson is **not** transitively included by `spring-boot-starter-webmvc` in Boot 4.x; `jackson-databind` is declared explicitly.
- The README references Gradle commands (`./gradlew bootRun`) — this project is Maven-only.
