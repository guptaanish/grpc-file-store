# SOLID Principles Rules

## Single Responsibility (SRP)

- If a service exceeds 300–500 lines, it should be decomposed.

## Open/Closed (OCP)

- Classes should be open for extension but closed for modification.

## Liskov Substitution (LSP)

- Subtypes must be replaceable for their base types without breaking behavior.

## Interface Segregation (ISP)

- Clients should not be forced to depend on interfaces they don't use.

## Dependency Inversion (DIP)

- Always depend on abstractions rather than concrete implementations.

## Core Architecture Principles

- **Statelessness** — Business services must be stateless.
- **Minimize Mutability** — Classes should be immutable unless there is a compelling reason otherwise.
- **Favor Composition over Inheritance** — Prefer delegating behavior to a new component.
- **Loose Coupling & High Cohesion** — Minimize inter-service dependencies, keep related logic together.
- **Domain-Driven Design** — Model services around bounded contexts and domain logic.
