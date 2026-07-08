---
name: refactor
description: Safely rename, move, or restructure code without breaking references or documentation.
---

# Refactor

Safely rename, move, or restructure code without breaking references or documentation.

## Procedure

1. **Identify scope**: List all classes, methods, or packages being renamed/moved.
2. **Find all references**: Search the entire project for usages — Java imports, `*.md` files, config files, proto files, shell scripts, Dockerfile, docker-compose.yml.
3. **Make the change**: Rename/move the code.
4. **Update all references**:
   - Java imports and usages.
   - README.md project structure and API table.
   - PROJECT.md architecture diagram and class references.
   - REQUIREMENTS.md if feature names changed.
   - application.yml if config property names changed.
   - Proto files if package or service names changed.
   - Javadoc on affected classes and methods.
   - Inline comments referencing old names.
5. **Verify no stale references**: Search project-wide for the old name — zero matches expected.
6. **Run tests**: `./gradlew build` must pass.
7. **Final check**: Grep all `*.md` files for the old name to confirm nothing was missed.

## Rules

- Never leave a stale reference to an old name anywhere in the project.
- If a class responsibility changes, update its class-level Javadoc immediately.
- If an endpoint path changes, update README.md API table.
- If a config property is renamed, update application.yml comments and any documentation referencing it.
- **Early-Return Guard Clauses** — When refactoring nested conditionals, invert negative checks to return/throw early, reducing nesting depth. Prefer `if (x == null) return;` over wrapping the entire method body in `if (x != null) { ... }`.
