# Spring Configuration Rules

## Externalized Configuration

- **Externalized Configuration** — Use `application.yml` for configuration; avoid hardcoded values.
- **Type-Safe Configuration** — Use `@ConfigurationProperties` with records for type-safe config binding.
- **Validation** — Validate configuration properties at startup with `@Validated` and Jakarta validation annotations.
- **Profile-Specific Config** — Use `application-{profile}.yml` for environment-specific settings.
