---
name: documentation
description: Verify that all documentation matches the current codebase state and fix any discrepancies.
---

# Documentation Sync

Verify that all documentation matches the current codebase state and fix any discrepancies.

## Procedure

1. Inventory all classes in `src/main/java/` and compare against README.md project structure.
2. Inventory all controller endpoints (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`) and compare against README.md API table.
3. Inventory all gRPC RPCs in proto files and compare against README.md API table (if applicable).
4. Check every feature in REQUIREMENTS.md is implemented (or marked as planned).
5. Check PROJECT.md architecture diagram matches actual components and version numbers.
6. Check for stale references in `*.md` files — no references to classes, packages, methods, files, or features that have been deleted, moved, or renamed.
7. Verify all Javadoc `@param`/`@return`/`@throws` match current method signatures.
8. Verify configuration properties documented match what exists in `application.yml`.
9. Verify shell script comments match their current behavior.
10. Verify Docker/nginx config comments match current setup.
11. Report findings and fix all discrepancies.

## Rules

- All `*.md` files must reflect the current state of the codebase.
- **Project structure** sections must list every package, class, and file that exists in `src/main/java/`.
- **API endpoint tables** must list every mapping in the controllers.
- **Feature lists** and **current state** checklists must match what is actually implemented.
- **Configuration properties** documented must match what exists in `application.yml`.
- **Prerequisites** (Java version, Node version, Docker) must match actual requirements.
- No references to old package names, class names, removed endpoints, deprecated features, or deleted configuration properties.
- Search the entire project for old names when moving or renaming code components.
