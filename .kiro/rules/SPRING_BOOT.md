# Spring Boot Rules

## Dependency Injection

- **Constructor Injection** — Always use constructor injection; avoid field injection (`@Autowired` on fields).
- **Final Fields** — Make injected dependencies `final` to ensure immutability.
- **Single Constructor** — If a class has only one constructor, `@Autowired` is optional (implicit).
- **Avoid `@Autowired` on Setters** — Setter injection makes dependencies optional, which is rarely desired.

## Configuration

- **Externalized Configuration** — Use `application.yml` for configuration; avoid hardcoded values.
- **Type-Safe Configuration** — Use `@ConfigurationProperties` with records for type-safe config binding.
- **Validation** — Validate configuration properties at startup with `@Validated` and Jakarta validation annotations.
- **Profile-Specific Config** — Use `application-{profile}.yml` for environment-specific settings.

## REST API Design

- **Consistent Endpoints** — Use RESTful conventions: `GET /resources`, `POST /resources`, `GET /resources/{id}`, `DELETE /resources/{id}`.
- **HTTP Status Codes** — Use appropriate codes: `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`, `500 Internal Server Error`.
- **Request/Response DTOs** — Use separate DTOs for requests and responses; don't expose domain models directly.
- **Validation** — Use `@Valid` on request bodies; return `400` with validation errors.
- **Error Responses** — Return consistent error response structure with timestamp, status, message, and path.

## Performance

- **Lazy Initialization** — Use `@Lazy` for expensive beans that aren't always needed.
- **Caching** — Use `@Cacheable`, `@CacheEvict`, `@CachePut` for expensive operations.
- **Async Processing** — Use `@Async` for fire-and-forget operations; configure thread pools appropriately.
- **Connection Pooling** — Configure connection pools for databases and HTTP clients.

## Logging & Observability

- Use **SLF4J** (`@Slf4j` from Lombok) as the logging facade.
- Use structured logging with placeholders: `log.info("Processing order id: {}", orderId);`
- Log levels:
  - `DEBUG` — Detailed flow, method entry/exit.
  - `INFO` — Major lifecycle events, significant business events.
  - `WARN` — Recoverable issues.
  - `ERROR` — Unrecoverable issues requiring intervention.
- Do **not** log the same event at the same level in both the controller and the service layer.
- **AOP for Cross-Cutting Concerns** — Use Spring Aspects for auditing or execution timing.
