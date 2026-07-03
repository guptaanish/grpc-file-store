# Testing Rules

## Test Structure

- **Naming Convention** — Test classes end with `Test` (unit) or `IntegrationTest` (integration).
- **Test Method Names** — Use descriptive names: `shouldReturnProfileWhenExists()`, `shouldThrowExceptionWhenProfileNotFound()`.
- **Arrange-Act-Assert** — Structure tests with clear setup, execution, and verification phases.
- **One Assertion Per Test** — Focus each test on a single behavior (exceptions: related assertions on same object).

## Test Coverage

- **Unit Tests** — Test business logic in isolation; mock dependencies.
- **Integration Tests** — Test full stack with `@SpringBootTest`; use test containers for external dependencies.
- **Edge Cases** — Test null inputs, empty collections, boundary conditions, and error paths.
- **No Test Logic** — Tests should be simple; avoid conditionals, loops, or complex logic in tests.

## Test Isolation

- **Independent Tests** — Each test must run independently; no shared mutable state.
- **Temporary Resources** — Use `@TempDir` for file operations; clean up after tests.
- **Test Configuration** — Use `@TestConfiguration` and `application-test.yml` to override production config.
