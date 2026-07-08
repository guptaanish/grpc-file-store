# Architecture & Design Rules

## Java Version & Modern Features

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

## SOLID & Single Responsibility

- **SRP** — If a service exceeds 300–500 lines, it should be decomposed.
- **OCP** — Classes should be open for extension but closed for modification.
- **LSP** — Subtypes must be replaceable for their base types without breaking behavior.
- **ISP** — Clients should not be forced to depend on interfaces they don't use.
- **DIP** — Always depend on abstractions rather than concrete implementations.

## Core Architecture Principles

- **Statelessness** — Business services must be stateless.
- **Minimize Mutability** — Classes should be immutable unless there is a compelling reason otherwise.
- **Favor Composition over Inheritance** — Prefer delegating behavior to a new component.
- **Loose Coupling & High Cohesion** — Minimize inter-service dependencies, keep related logic together.
- **Domain-Driven Design** — Model services around bounded contexts and domain logic.

## Class Structure & Visibility

- Make every class or member as private as possible.
- Fields must be private.
- Use Constructor Injection (which allows fields to be final).
- Default methods to private. Only expose public methods that represent the class's contract.
- **Static Factory Methods** — Prefer these over constructors.

## Design Patterns & Complexity

- **Strategy Pattern** — Replace complex if-else or switch blocks.
- **Template Method** — Use abstract classes to define workflow.
- **Builder Pattern** — Essential for complex object creation.
- **Proxy Pattern Awareness** — Avoid self-invocation with `@Transactional`/`@Cacheable`.
- **Cyclomatic Complexity** — Aim for a score below 10 per method.

## Code Smells to Avoid

- **God Classes** — Classes with >500 lines or >10 dependencies.
- **Long Methods** — Methods exceeding 50 lines should be refactored.
- **Deep Nesting** — Avoid nesting beyond 3 levels.
- **Magic Numbers** — Use named constants.
- **Commented-Out Code** — Delete it; version control preserves history.
- **Primitive Obsession** — Use domain objects instead of primitives.
- **Feature Envy** — If a method uses more data from another class than its own, it belongs in that class.
