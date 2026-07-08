# API Versioning

Workflow for evolving APIs without breaking existing clients, applicable to REST, gRPC, and event-driven systems.

## Steps

1. **Assess the change type** — Classify the API modification:
   - **Backward-compatible (non-breaking)**: Adding new fields, new endpoints/RPCs, new optional parameters
   - **Breaking**: Removing fields, renaming fields, changing types, changing semantics, removing endpoints
   - **Behavioral**: Changing response format, altering error codes, modifying pagination

2. **Choose versioning strategy** — Based on API type and project conventions:
   - **gRPC/Protobuf**: Package versioning (`package myservice.v1;` → `myservice.v2;`), field deprecation with `reserved`
   - **REST**: URL path versioning (`/api/v1/`), header versioning (`Accept: application/vnd.api.v2+json`), or query param
   - **Events**: Schema versioning with a version field in the event payload
   - Prefer evolving the existing version with additive changes over creating a new version

3. **Apply backward-compatible changes safely** — For non-breaking changes:
   - Add new fields as optional (protobuf fields are optional by default in proto3)
   - Never reuse removed field numbers in protobuf (use `reserved`)
   - New REST fields should have sensible defaults or be nullable
   - New endpoints/RPCs can be added freely
   - Document new fields/endpoints in API docs and changelogs

4. **Plan breaking changes** — When a breaking change is unavoidable:
   - Create a new API version (new package, new URL prefix)
   - Maintain the old version for a documented deprecation period
   - Provide migration guide for clients
   - Add deprecation notices (`@Deprecated`, proto `deprecated = true` option)
   - Set a sunset date and communicate it

5. **Implement version coexistence** — Run old and new versions simultaneously:
   - Share business logic between versions (only the API layer differs)
   - Use adapters/mappers to translate between version-specific DTOs and internal models
   - Route requests to appropriate handler based on version
   - Ensure database schema supports both versions

6. **Update client contracts and documentation** — For every version change:
   - Regenerate client SDKs/stubs if applicable
   - Update OpenAPI spec / proto files
   - Update API documentation with version-specific examples
   - Add changelog entry describing what changed and why

7. **Test version compatibility** — Verify:
   - Old clients still work against the updated service (backward compatibility)
   - New clients work with the new version
   - No data corruption when both versions operate on shared state
   - Error responses are version-appropriate

8. **Deprecate and sunset old versions** — When ready to remove:
   - Log usage metrics on old version to confirm migration
   - Return deprecation warnings in responses (headers, response fields)
   - Remove old version after sunset period expires
   - Clean up dead code, old DTOs, unused database columns
