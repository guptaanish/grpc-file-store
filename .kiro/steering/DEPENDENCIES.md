# Dependency Management Rules

## Version Pinning

- **Pin all dependency versions** — Use exact versions, not dynamic ranges (`1.5.+`, `latest.release`).
- Centralize version numbers in one place. This project declares them as `val` constants at the top of `build.gradle.kts`; a Gradle version catalog (`gradle/libs.versions.toml`) is the preferred approach if the dependency set grows.
- Use Spring Boot's dependency management BOM for managed dependencies.
- Override BOM versions only with justification (security fix, bug fix).

## Dependency Selection

- Prefer well-known, actively maintained libraries with strong community support.
- Check last release date — avoid libraries with no release in 12+ months.
- Prefer libraries that align with the Spring ecosystem (Spring-managed versions).
- Avoid duplicate functionality — do not add a library that duplicates what's already on the classpath.
- Document the reason for non-obvious dependencies in build comments.

## Security & Vulnerability Scanning

- Run dependency vulnerability checks in CI when a scanner is configured (e.g., the OWASP `dependencyCheckAnalyze` task, Gradle's `dependencies` audit, or an equivalent). This project does not yet wire one in — adding it is a recommended hardening step.
- Address CRITICAL and HIGH CVEs within one sprint; MEDIUM within the quarter.
- Subscribe to security advisories for critical dependencies.
- Review transitive dependencies — a direct dependency may pull in vulnerable transitives.

## Build Hygiene

- No `SNAPSHOT` dependencies in release branches.
- Remove unused dependencies — dead dependencies increase attack surface and build time.
- Use `implementation` (not `api`) for dependencies that should not leak to consumers.
- Separate test dependencies with `testImplementation`.
- Use `compileOnly` for annotation processors and compile-time-only libraries.

## Upgrade Strategy

- Update dependencies regularly (at least monthly for patches, quarterly for minors).
- Read changelogs and migration guides before upgrading major versions.
- Run the full test suite after any dependency change.
- Upgrade one dependency at a time to isolate breakage.
- Use Dependabot, Renovate, or equivalent for automated update PRs.