# Class Design Rules

## Structure & Visibility

- Make every class or member as private as possible.
- Fields must be private.
- Use Constructor Injection (which allows fields to be final).
- Default methods to private. Only expose public methods that represent the class's contract.
- **Static Factory Methods** — Prefer these over constructors.
