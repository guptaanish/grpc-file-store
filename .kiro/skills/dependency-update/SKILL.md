---
name: dependency-update
description: Procedure for safely updating a project dependency.
---

# Dependency Update

Procedure for safely updating a project dependency.

## Procedure

1. **Identify the dependency**: Determine which dependency to update and the target version.
2. **Check changelog/release notes**: Review breaking changes, deprecations, and migration steps.
3. **Update version**: Modify `build.gradle.kts` version constants or `gradle/wrapper/gradle-wrapper.properties` for Gradle itself.
4. **Update documentation**: Update README.md technology stack with the new version.
5. **Rebuild**: Run `./gradlew build` to confirm compilation and tests pass.
6. **Check for deprecation warnings**: Review build output for new warnings introduced by the update.
7. **Verify runtime**: Start the application and smoke test key functionality.

## Rules

- Always pin exact versions — no open ranges.
- Update README.md immediately after changing a version.
- If a breaking change requires code modifications, make them in the same change.
