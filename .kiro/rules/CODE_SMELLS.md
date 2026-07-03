# Code Smells Rules

## Smells to Avoid

- **God Classes** — Classes with >500 lines or >10 dependencies.
- **Long Methods** — Methods exceeding 50 lines should be refactored.
- **Deep Nesting** — Avoid nesting beyond 3 levels.
- **Magic Numbers** — Use named constants.
- **Commented-Out Code** — Delete it; version control preserves history.
- **Primitive Obsession** — Use domain objects instead of primitives.
- **Feature Envy** — If a method uses more data from another class than its own, it belongs in that class.
