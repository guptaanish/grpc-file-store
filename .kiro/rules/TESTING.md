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

## Coverage Gate

- Coverage is measured with **JaCoCo** (`./gradlew test` finalizes with `jacocoTestReport`; the
  `coverageMap` task produces per-test → production-class mapping).
- Treat coverage as a floor, not a goal: aim for **≥ 80% line** and **≥ 70% branch** coverage on
  `com.example.filestore.*`, allowing pragmatic exclusions for generated code (proto stubs, MapStruct
  impls) and trivial getters.
- Prefer enforcing the floor with a `jacocoTestCoverageVerification` rule wired into `check` so
  regressions fail the build, rather than relying on manual report review.
- Coverage percentage alone is insufficient — assert on behavior and edge cases, not just execution.
  Do not add assertion-free tests to inflate the number.
- New or changed code should not lower overall coverage; add tests in the same change.

## Test Isolation

- **Independent Tests** — Each test must run independently; no shared mutable state.
- **Temporary Resources** — Use `@TempDir` for file operations; clean up after tests.
- **Test Configuration** — Use `@TestConfiguration` and `application-test.yml` to override production config.
