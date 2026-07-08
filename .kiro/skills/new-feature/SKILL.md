---
name: new-feature
description: End-to-end workflow for implementing a new feature across backend, frontend, and documentation.
---

# New Feature

End-to-end workflow for implementing a new feature across backend, frontend, and documentation.

## Procedure

1. **Update REQUIREMENTS.md**: Add the feature requirement (FR or NFR) with acceptance criteria.
2. **Design**: Determine which packages/classes are affected. Create new classes if needed following existing architecture patterns.
3. **Implement backend**:
   - Add/modify service classes with business logic.
   - Add/modify controller endpoints (follow new-endpoint skill if adding REST endpoints).
   - Add/modify gRPC RPC methods or proto definitions as needed.
   - Add metrics if the feature is observable (`Counter`, `Timer`, `Gauge`).
   - Ensure thread safety for any shared mutable state.
4. **Implement frontend** (if UI-facing):
   - Add/modify React components or pages.
   - Wire up API calls and SSE events if real-time.
5. **Write tests**: Unit tests for services, integration tests for endpoints.
6. **Update documentation**:
   - README.md — project structure, API table, any new prerequisites.
   - PROJECT.md — architecture diagram, technology stack, current state checklist.
   - REQUIREMENTS.md — mark feature as implemented.
   - Proto file comments for new RPCs/messages (if gRPC).
7. **Verify build**: Run `./gradlew build` and `cd frontend && npm run build`.

## Rules

- Follow existing patterns — don't introduce new libraries or architectural styles without discussion.
- Every new class needs full Javadoc (class, fields, methods).
- Every new endpoint needs a test and a README API table entry.
- Every new gRPC RPC needs an integration test and a README API table entry.
- SSE events must be broadcast for any state change visible in the UI.
