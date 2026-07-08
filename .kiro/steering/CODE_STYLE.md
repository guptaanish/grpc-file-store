# Code Style Rules

## General

- Ensure all code is **production-ready**: readable, maintainable, with proper error handling and performance considerations.
- Use Java `record` types wherever applicable to represent immutable data carriers and reduce boilerplate.
- Use **Lombok** annotations (`@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`, etc.) where appropriate to minimize boilerplate.
- Avoid "Lombok-everything"; use `@Getter` and `@Setter` only where necessary.

## Development Workflow Checklist

- Use `final` for local variables that don't change to aid JVM optimization.
- No `System.out.println` or `printStackTrace()`.
- Always use try-with-resources for I/O operations.
- Use `@ControllerAdvice` for global error handling; never swallow exceptions.
- Use `@Valid` and `@Validated` at the API layer.

## File Reading Before Modification

- **Always** read the latest contents of a file from disk before making any code change.
- Do **not** rely on previously cached or in-memory versions of a file.

## Documentation Sync

- After **every** code change, review and update **all** `*.md` files to reflect the current state.
- **MANDATORY**: Verify that **no stale references** exist to classes, packages, methods, files, or features that have been deleted, moved, or renamed.
- Use project-wide search to identify and update all references when moving or renaming code components.
