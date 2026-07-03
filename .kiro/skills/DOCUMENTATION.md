# Documentation Sync

Verify that all documentation matches the current codebase state and fix any discrepancies.

## Procedure

1. Inventory all classes in `src/main/java/` and compare against README.md project structure.
2. Inventory all gRPC RPCs in the proto file and compare against README.md API table.
3. Check every feature in REQUIREMENTS.md is implemented (or marked as planned).
4. Check for stale references in `*.md` files — no references to classes, packages, methods, files, or features that have been deleted, moved, or renamed.
5. Verify all Javadoc `@param`/`@return`/`@throws` match current method signatures.
6. Verify configuration properties documented match what exists in `application.yml`.
7. Report findings and fix all discrepancies.

## Rules

- All `*.md` files must reflect the current state of the codebase.
- **Project structure** sections must list every package and class that exists in `src/main/java/`.
- **API tables** must list every RPC in the proto service definition.
- **Configuration properties** documented must match what exists in `application.yml`.
- No references to old package names, class names, removed RPCs, deprecated features, or deleted configuration properties.
- Search the entire project for old names when moving or renaming code components.
