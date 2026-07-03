# Logging & Observability Rules

## Logging Facade

- Use **SLF4J** (`@Slf4j` from Lombok) as the logging facade.
- Use structured logging with placeholders: `log.info("Processing order id: {}", orderId);`

## Log Levels

- `DEBUG` — Detailed flow, method entry/exit.
- `INFO` — Major lifecycle events, significant business events.
- `WARN` — Recoverable issues.
- `ERROR` — Unrecoverable issues requiring intervention.

## Best Practices

- Do **not** log the same event at the same level in both the controller and the service layer.
- **AOP for Cross-Cutting Concerns** — Use Spring Aspects for auditing or execution timing.
