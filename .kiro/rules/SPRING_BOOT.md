# Spring Boot Rules

## Dependency Injection

- **Constructor Injection** — Always use constructor injection; avoid field injection (`@Autowired` on fields).
- **Final Fields** — Make injected dependencies `final` to ensure immutability.
- **Single Constructor** — If a class has only one constructor, `@Autowired` is optional (implicit).
- **Avoid `@Autowired` on Setters** — Setter injection makes dependencies optional, which is rarely desired.
- **`@RequiredArgsConstructor`** — Prefer Lombok's `@RequiredArgsConstructor` over explicit constructors for classes with only `final` fields.
- **`@Qualifier`** — Use `@Qualifier` annotations when multiple beans of the same interface exist; inject by qualifier name, not by field name.

## Configuration

- **Externalized Configuration** — Use `application.yml` for configuration; avoid hardcoded values.
- **Type-Safe Configuration** — Use `@ConfigurationProperties` with records for type-safe config binding.
- **Validation** — Validate configuration properties at startup with `@Validated` and Jakarta validation annotations.
- **Profile-Specific Config** — Use `application-{profile}.yml` for environment-specific settings.
- **Custom Validators** — Create custom constraint annotations (e.g., `@WritableDirectory`) for domain-specific configuration validation that runs at startup.
- **`@Value` for Simple Properties** — Use `@Value` for single-property injection only; prefer `@ConfigurationProperties` records for groups of related properties.

## Global Exception Handling

- **`@RestControllerAdvice`** — Use a single `GlobalExceptionHandler` class annotated with `@RestControllerAdvice` to map all domain exceptions to HTTP responses.
- **Decompose Responsibilities** — Extract error logging and error response building into separate components (`ErrorLogger`, `ErrorResponseBuilder`) following the Single Responsibility Principle. The exception handler orchestrates; it does not log or build responses directly.
- **Consistent Error Format** — All error responses must use a standardized JSON structure (e.g., `{"error": "message"}`). A centralized `ErrorResponseBuilder` ensures format consistency and makes future schema changes (adding timestamps, error codes, request IDs) single-point modifications.
- **Exception-to-Status Mapping** — Establish a clear, documented mapping from domain exceptions to HTTP status codes:
  - `NotFoundException` variants → `404 Not Found`
  - `IllegalArgumentException`, validation exceptions, content validation exceptions → `400 Bad Request`
  - `ConstraintViolationException` (Bean Validation) → `400 Bad Request`
  - `MethodArgumentNotValidException` (`@Valid` failures) → `400 Bad Request`
  - Repository/infrastructure exceptions → `500 Internal Server Error`
- **Never Expose Internals in 500s** — For server errors, return a generic message (e.g., `"Failed to process profile operation"`) and log the full stack trace server-side. Never leak exception messages, class names, or stack traces to the client.
- **Log at the Handler, Not the Controller** — Controllers should not catch and log exceptions. Let them propagate to the global handler where they are logged once in a consistent format.
- **Sanitize Before Logging** — Always sanitize user-controlled values through `LogSanitizer` (or equivalent) before writing to logs to prevent CRLF log injection.

## Error Logging Component

- **Centralized Error Logger** — Encapsulate all exception logging in a dedicated `@Component` class (e.g., `ErrorLogger`). This isolates logging format decisions from exception-handling logic.
- **Security in Logging** — Sanitize all user input before logging. Log full stack traces only for server errors (5xx), not for client errors (4xx).
- **Return Sanitized Messages** — The error logger may return the client-safe message string so the handler can pass it directly to the response builder without re-extracting it.

## Error Response Builder

- **Stateless `@Component`** — The error response builder is a stateless component that constructs `ResponseEntity<Map<String, String>>` (or a dedicated error DTO) with the appropriate HTTP status.
- **Factory Methods per Status** — Provide convenience methods: `badRequest(message)`, `notFound(message)`, `internalServerError(message)`, etc.
- **Extensibility** — The builder is the single place to add fields like `timestamp`, `path`, `errorCode`, or `requestId` to error responses in the future.

## REST API Design

- **Consistent Endpoints** — Use RESTful conventions: `GET /resources`, `POST /resources`, `GET /resources/{id}`, `DELETE /resources/{id}`.
- **HTTP Status Codes** — Use appropriate codes: `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`, `500 Internal Server Error`.
- **Request/Response DTOs** — Use Java `record` types for request and response DTOs; don't expose domain models directly.
- **Validation at the Boundary** — Use `@Valid` on `@RequestBody` parameters and `@Validated` on the controller class for path/query parameter validation.
- **Custom Constraints** — Define custom constraint annotations (e.g., `@ValidProfileName`) for domain-specific path variable validation.
- **Path Variable Security** — Always sanitize path variables through a `PathSanitizer` (or equivalent) before using them in file system or database operations to prevent path traversal attacks.
- **OpenAPI Documentation** — Annotate controllers with `@Tag`, methods with `@Operation` and `@ApiResponse` for Swagger UI generation via springdoc.
- **Error Responses** — Return consistent error response structure from the `GlobalExceptionHandler`. Individual controllers should not build their own error responses.

## Content Validation

- **Layered Validation** — Validate in layers: (1) Bean Validation at the API boundary (`@NotNull`, `@NotBlank`, `@Size`), (2) custom constraint annotations on path variables, (3) domain-specific content validators in the service/controller layer (e.g., XML schema validation).
- **Validator Interface** — Define a `ProfileValidator` (or equivalent) interface and implement per content type. Validators are `@Component` beans injected into controllers.
- **Fail Before Persistence** — Always validate content before saving. Call content validators explicitly after `@Valid` deserialization passes.

## Servlet Filters

- **MDC Filter** — Use a `OncePerRequestFilter` at `Ordered.HIGHEST_PRECEDENCE` to populate the SLF4J MDC with request metadata (request ID, method, URI, app name, version, hostname, duration).
- **Request ID Propagation** — Accept an `X-Request-ID` header if present; otherwise generate a UUID. Always echo the request ID back in the response header.
- **Always Clear MDC** — Use a `try/finally` block to ensure `MDC.clear()` runs after every request to prevent thread-local leakage.
- **Request Logging** — Log the completed request (method, URI, status, duration) at INFO level from the MDC filter, not from individual controllers.

## Service Layer

- **Thin Controllers, Rich Services** — Controllers handle HTTP concerns (validation, response wrapping). Business logic lives in `@Service` classes.
- **Repository Abstraction** — Services depend on repository interfaces, not implementations. Use `@Qualifier` when multiple implementations exist.
- **Domain Exceptions** — Services throw domain-specific exceptions (e.g., `ProfileNotFoundException`, `ProfileRepositoryException`). Never throw generic `RuntimeException`.
- **Log Sanitization in Services** — Sanitize user-provided values when logging at DEBUG level in services, same as in controllers.

## Performance

- **Lazy Initialization** — Use `@Lazy` for expensive beans that aren't always needed.
- **Caching** — Use `@Cacheable`, `@CacheEvict`, `@CachePut` for expensive operations.
- **Startup Caching** — For immutable resources (e.g., classpath files), load them once in `@PostConstruct` and cache in a field.
- **Async Processing** — Use `@Async` for fire-and-forget operations; configure thread pools appropriately.
- **Connection Pooling** — Configure connection pools for databases and HTTP clients.

## Logging & Observability

- Use **SLF4J** (`@Slf4j` from Lombok) as the logging facade.
- Use structured logging with placeholders: `log.info("Processing order id: {}", orderId);`
- Log levels:
  - `DEBUG` — Detailed flow, method entry/exit (service layer).
  - `INFO` — Request received/completed, major lifecycle events (controller + filter layer).
  - `WARN` — Recoverable issues.
  - `ERROR` — Unrecoverable issues, exception handling (global exception handler only).
- Do **not** log the same event at the same level in both the controller and the service layer. Controllers log at INFO; services log at DEBUG.
- **AOP for Cross-Cutting Concerns** — Use Spring Aspects for auditing or execution timing.
- **Log Injection Prevention** — All user-controlled strings must be sanitized via a `LogSanitizer` utility before being interpolated into log messages (escapes `\n`, `\r`).

## CORS Configuration

- **Explicit Origins** — Never use wildcard (`*`) for allowed origins. List specific trusted origins from configuration.
- **Externalize Origins** — Read allowed origins from `application.yml` so they can change per environment without code changes.
- **Scoped Mappings** — Apply CORS only to API paths (`/api/**`), not to static resources or actuator endpoints.

## SPA Serving (Optional)

- **Fallback to `index.html`** — When serving a React/Angular/Vue frontend from `classpath:/static/`, configure a `PathResourceResolver` that falls back to `index.html` for non-API, non-actuator, non-Swagger paths.
- **Exclude System Paths** — Explicitly exclude `/api/`, `/actuator/`, `/swagger-ui` from the SPA fallback so they are handled by their respective Spring handlers.
