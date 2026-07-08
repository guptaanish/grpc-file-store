---
name: dependency-update
description: Procedure for safely updating a project dependency.
---

# Dependency Update

Procedure for safely updating a project dependency.

## Procedure

1. **Identify the dependency**: Determine which dependency to update and the target version.
2. **Check changelog/release notes**: Review breaking changes, deprecations, and migration steps.
3. **Update version**: Modify `backend/gradle/libs.versions.toml` (or `docker-compose.yml` for Docker images, `frontend/package.json` for npm).
4. **Update PROJECT.md**: Update the technology stack table with the new version.
5. **Update comments**: If any config file comments reference the old version, update them.
6. **Rebuild**: Run `./gradlew build` (backend) or `npm run build` (frontend).
7. **Run tests**: Verify all tests pass with the new version.
8. **Check for deprecation warnings**: Review build output for new warnings introduced by the update.
9. **Verify runtime**: If a Docker image changed, run `docker compose up --build -d` and smoke test.

## Rules

- Always pin exact versions — no open ranges.
- Update PROJECT.md technology stack table immediately after changing a version.
- If a Docker image version changes, update docker-compose.yml comments referencing it.
- If a breaking change requires code modifications, make them in the same change.
