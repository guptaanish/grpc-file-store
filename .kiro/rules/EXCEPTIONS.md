# Exception Handling Rules

## Exception Design

- **Use Specific Exceptions** — Create custom exceptions for domain-specific errors.
- **Exception Hierarchy** — Extend from appropriate base exceptions (`RuntimeException` for unchecked, `Exception` for checked).
- **Immutable Exceptions** — Exception classes should be immutable; use `final` fields and constructor-only initialization.
- **Meaningful Messages** — Exception messages must be clear, actionable, and include context.

## Exception Handling Patterns

- **Never Swallow Exceptions** — Always log or rethrow; never catch and ignore without justification.
- **Catch Specific Exceptions** — Avoid catching `Exception` or `Throwable`; catch specific types.
- **Global Exception Handling** — Use `@ControllerAdvice` for REST API exception handling; return consistent error responses.
- **Fail Fast** — Validate inputs early and throw exceptions immediately rather than propagating invalid state.
- **Don't Use Exceptions for Control Flow** — Exceptions are for exceptional conditions, not normal program flow.

## Resource Management

- **Try-With-Resources** — Always use try-with-resources for `AutoCloseable` resources (files, streams, connections).
- **No Manual Close** — Avoid manual `finally` blocks for resource cleanup when try-with-resources is available.
