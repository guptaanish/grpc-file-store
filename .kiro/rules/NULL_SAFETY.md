# Null Safety Rules

## Avoiding Null

- **Avoid Null Returns** — Return `Optional<T>`, empty collections, or use `@Nullable`/`@NonNull` annotations.
- **Optional Usage** — Use `Optional` for return types where absence is expected; avoid `Optional` as method parameters or fields.
- **Null Checks** — Use `Objects.requireNonNull()` for parameter validation in constructors and public methods.
- **Collections** — Return empty collections (`List.of()`, `Collections.emptyList()`) instead of `null`.
