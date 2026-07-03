# Java 21 Features Rules

## Modern Language Features

- This project uses **Java 21**. Leverage modern Java features where appropriate:
  - **Records** — Use for immutable data carriers (DTOs, domain models, configuration properties).
  - **Pattern Matching** — Use `instanceof` pattern matching and switch expressions.
  - **Text Blocks** — Use for multi-line strings (SQL, JSON, XML templates).
  - **Sealed Classes** — Use for restricted class hierarchies when appropriate.
  - **Virtual Threads** — Prefer Spring's `@Async` with virtual threads over manual thread management.
- Do **not** use legacy patterns when modern alternatives exist.

## Local Variables

- Use `var` for local variable declarations when the type is obvious from the right-hand side.
- Do **not** use `var` when the type is ambiguous or when it reduces readability.
