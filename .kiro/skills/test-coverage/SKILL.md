# Test Coverage

Workflow for identifying test coverage gaps and writing missing tests to improve code quality and reliability.

## Steps

1. **Run existing tests with coverage** — Execute the test suite with coverage reporting:
   - `./gradlew test jacocoTestReport` (Gradle/JaCoCo)
   - `mvn test jacoco:report` (Maven/JaCoCo)
   - If no coverage tool is configured, add JaCoCo plugin to the build

2. **Analyze coverage report** — Review the generated report:
   - Identify classes/packages below target threshold (aim for 80%+ line coverage on business logic)
   - Focus on **branch coverage** (not just line coverage) — untested conditional paths are where bugs hide
   - Prioritize: service layer > controllers/gRPC handlers > utilities > configuration

3. **Identify critical gaps** — Flag untested areas by risk:
   - **High priority**: Error handling paths, security logic, data validation, concurrency code
   - **Medium priority**: Edge cases (null inputs, empty collections, boundary values), integration points
   - **Low priority**: Simple getters/setters, configuration beans, framework-generated code

4. **Write missing unit tests** — For each gap:
   - Follow existing test conventions (naming, structure, assertions library)
   - Use Arrange-Act-Assert pattern
   - Mock external dependencies with Mockito
   - Test both happy path and failure scenarios
   - Cover boundary conditions and edge cases

5. **Write missing integration tests** — For untested integration points:
   - Use `@SpringBootTest` for full-stack tests
   - Use Testcontainers for external dependencies (DB, message brokers)
   - Test end-to-end request/response flows
   - Verify error responses and status codes

6. **Verify mutation testing (optional)** — Run mutation testing to validate test quality:
   - Use PIT (`pitest`) to check if tests actually catch code changes
   - Focus on surviving mutants in critical business logic
   - A high line coverage with low mutation score indicates weak assertions

7. **Re-run coverage and validate** — After adding tests:
   - Confirm coverage improved to target threshold
   - Ensure no existing tests broke
   - Verify new tests are deterministic (no flakiness)

8. **Configure coverage enforcement** — If not already present:
   - Add JaCoCo `violationRules` to fail the build below threshold
   - Exclude generated code, config classes, and DTOs from coverage requirements
   - Document agreed coverage thresholds in the project
