# Spring Boot Rules

Spring Boot conventions for this service. This project is a **gRPC service** (server on port
`9090`) with an HTTP surface limited to the actuator (port `8080`). Prefer gRPC interceptors over
servlet filters and `@RestControllerAdvice` for cross-cutting concerns.

This file is the Spring Boot entry point. To avoid drift, it **references** the focused rule files
rather than restating them:

- Dependency injection → `DEPENDENCY_INJECTION.md`
- Configuration properties → `SPRING_CONFIGURATION.md`
- Performance (caching, `@Async`, `@Lazy`, pooling) → `SPRING_PERFORMANCE.md`
- Logging → `LOGGING.md`; metrics/tracing/health → `OBSERVABILITY.md`
- Exceptions → `EXCEPTIONS.md`; gRPC status mapping → `GRPC.md`
- Transactions & JPA → `DATABASE.md`
- REST endpoints (if/when added) → `REST_API.md` and `API_DESIGN.md`
- Resilience & graceful shutdown → `RESILIENCE.md`
- Auth & transport security → `AUTHENTICATION.md`

## Beans & Application Structure

- Favor constructor injection with `final` fields (see `DEPENDENCY_INJECTION.md`); prefer Lombok's
  `@RequiredArgsConstructor` for classes whose dependencies are all `final`.
- Business logic lives in `@Service` beans; gRPC service classes (`@GrpcService`) are the transport
  layer and should stay thin — validate, delegate, map, respond.
- Services depend on interfaces (e.g., `PaymentService`, `NotificationService`), never on
  concrete implementations. Use `@Qualifier` by name when multiple implementations exist.
- Keep beans stateless (see `SOLID_PRINCIPLES.md`). Request-scoped state belongs in the gRPC
  `Context`, not in bean fields.

## Conditional & Auto-Configuration

- Use `@ConditionalOnProperty` to select beans from configuration (e.g., choosing a storage
  backend based on a property). Default to a safe, explicit backend.
- Provide a sensible fallback with `@ConditionalOnMissingBean` when defining optional beans.
- Keep conditional wiring in `@Configuration` classes, not scattered across `@Service` beans.

## Configuration & Profiles

- Bind related properties with `@ConfigurationProperties` records and validate them at startup with
  `@Validated` + Jakarta constraints (see `SPRING_CONFIGURATION.md`). Prefer failing startup over
  running with invalid config.
- Use `@Value` only for a single unrelated property; group related properties into a record.
- Use custom constraint annotations (e.g., a `@WritableDirectory` check on the storage directory)
  for domain-specific startup validation.
- Keep environment differences in `application-{profile}.yml`. Insecure conveniences — plaintext
  gRPC, reflection, `ddl-auto: update`, `h2-console`, `health show-details: always` — must be
  scoped to a local/dev profile and off by default in production (see `AUTHENTICATION.md`,
  `DATABASE.md`).

## Bean Lifecycle

- Do expensive one-time setup in `@PostConstruct` (e.g., cache immutable classpath resources); do
  not repeat it per request.
- Release resources in `@PreDestroy`; combine with graceful shutdown so in-flight calls drain.

## Exception Handling (gRPC)

- Do **not** add `@RestControllerAdvice` for the gRPC path — it does not apply. Map domain
  exceptions to gRPC `Status` in a centralized exception-handling interceptor (see `GRPC.md`).
- Never expose stack traces, class names, or internal messages in `Status` descriptions; log the
  full detail server-side once, at the interceptor, and return a generic message for `INTERNAL`.
- Throw specific domain exceptions from services (e.g., `NotFoundException`,
  `QuotaExceededException`), never bare `RuntimeException` (see `EXCEPTIONS.md`).
- If a REST/actuator-adjacent endpoint is ever added, use a single `@RestControllerAdvice` there —
  see `REST_API.md`.

## Actuator & Management

- Expose only the endpoints you need (`health`, `info`, `metrics`); do not expose everything.
- Prefer a separate management port or network exposure controls in production so operational
  endpoints are not reachable by untrusted clients.
- `health show-details: always` is for local/dev only — restrict details in production to avoid
  leaking dependency topology.
- Implement liveness and readiness separately and back custom `HealthIndicator`s with lightweight
  checks (see `OBSERVABILITY.md`, `RESILIENCE.md`).

## Graceful Shutdown

- Enable `server.shutdown=graceful` for the HTTP/actuator server and configure a shutdown timeout.
- Ensure the gRPC server drains in-flight calls on `SIGTERM` before closing (see `RESILIENCE.md`).
- Flush logs and close storage resources in order during shutdown.

## Testing

- Use the narrowest test slice that covers the behavior:
  - `@DataJpaTest` for repository/JPA query tests.
  - Plain unit tests (no Spring context) for service logic with mocked collaborators.
  - `@SpringBootTest` for full gRPC round-trips through the real interceptor chain.
- Override production config with `application-test.yml` / `@TestConfiguration`; never mutate shared
  state between tests (see `TESTING.md`).
- Reserve `@MockBean`/`@SpyBean` for the boundary you are isolating; overuse couples tests to wiring.

## Security Defaults

- Default to secure: a production profile with auth/TLS unset should fail fast, not silently run
  open (see `AUTHENTICATION.md`).
- Sanitize user-controlled values before logging to prevent CRLF log injection (see `SECURITY.md`,
  `LOGGING.md`).
- Validate and reject unsafe file paths (`..`, absolute paths, separators) before any filesystem
  operation (see `SECURITY.md`).
