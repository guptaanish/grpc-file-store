# Code Style Rules

## General

- Ensure all code is **production-ready**: readable, maintainable, with proper error handling and performance considerations.
- Use Java `record` types wherever applicable to represent immutable data carriers and reduce boilerplate.
- Use **Lombok** annotations (`@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`, etc.) where appropriate to minimize boilerplate.
- Avoid "Lombok-everything"; use `@Getter` and `@Setter` only where necessary.

## Iteration: Prefer Streams & Lambdas

- **Always prefer Streams and lambdas** over traditional `for` / `for-each` loops for collection processing.
- Use Streams for: filtering, mapping, reducing, collecting, grouping, sorting, and flat-mapping.
- Use `for` loops **only** when Streams cannot be used or are clearly inappropriate:
  - Early exit with side effects that cannot be expressed with `findFirst()`/`anyMatch()`.
  - Index-based iteration where the index is consumed (e.g., parallel array manipulation).
  - Performance-critical tight loops where Stream overhead is measured and unacceptable.
  - Accumulating into a mutable structure that Streams cannot target cleanly (rare).
- Prefer method references (`ClassName::method`) over lambdas when the lambda body is a single method call.
- Keep lambda bodies short (≤ 3 lines); extract longer logic into a named private method.
- Use `Collectors.toUnmodifiableList()` / `Collectors.toUnmodifiableMap()` to produce immutable results.
- Avoid `.forEach()` with side effects as a replacement for a `for` loop — use `for-each` if the only purpose is side effects and no Stream pipeline precedes it.

```java
// BAD — traditional for loop for transformation
List<String> names = new ArrayList<>();
for (User user : users) {
    if (user.isActive()) {
        names.add(user.getName().toUpperCase());
    }
}

// GOOD — Stream pipeline
List<String> names = users.stream()
        .filter(User::isActive)
        .map(User::getName)
        .map(String::toUpperCase)
        .collect(Collectors.toUnmodifiableList());
```

## Development Workflow Checklist

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
