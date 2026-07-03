---
name: new-feature
description: End-to-end workflow for implementing a new feature across backend, documentation, and tests.
---

# New Feature

End-to-end workflow for implementing a new feature.

## Procedure

1. **Design**: Determine which packages/classes are affected. Create new classes if needed following existing architecture patterns.
2. **Implement**:
   - Add/modify service classes with business logic.
   - Add/modify gRPC RPC methods or proto definitions as needed.
   - Add metrics if the feature is observable.
   - Ensure thread safety for any shared mutable state.
3. **Write tests**: Unit tests for services, integration tests for gRPC round-trips.
4. **Update documentation**:
   - README.md — project structure, API table, any new prerequisites.
   - Proto file comments for new RPCs/messages.
5. **Verify build**: Run `./gradlew build` — must compile, pass all tests.

## Rules

- Follow existing patterns — don't introduce new libraries or architectural styles without discussion.
- Every new class needs full Javadoc (class, fields, methods).
- Every new RPC needs an integration test and a README API table entry.
