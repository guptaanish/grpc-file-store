# API Design Rules

## General Principles

- **Consistency** — All APIs (REST, gRPC) must follow the same patterns for similar operations.
- **Discoverability** — APIs should be self-describing; use clear naming and documentation.
- **Backward Compatibility** — Additive changes only; never remove or rename fields in an existing version.
- **Least Surprise** — APIs should behave as developers expect based on naming and conventions.

## Pagination

- All list/search endpoints **must** support pagination.
- Use cursor-based pagination (`page_token` / `next_page_token`) for gRPC and large datasets.
- Use offset-based pagination (`page` / `size`) only for REST APIs with small, stable datasets.
- Return an empty list (not an error) when no results match.
- Set sensible default and maximum page sizes (e.g., default 20, max 100).

## Request Validation

- Validate all required fields at the API boundary before reaching business logic.
- Return descriptive error messages indicating which field failed and why.
- Validate field formats (UUID format, email format, string length) at the entry point.
- Reject unknown/unexpected fields in strict mode or ignore them gracefully.

## Idempotency

- `GET`, `HEAD`, `OPTIONS` — Always idempotent and safe (no side effects).
- `PUT`, `DELETE` — Must be idempotent (repeated calls produce the same result).
- `POST` / client-streaming RPCs — Use idempotency keys or natural deduplication where possible.
- Document which operations are idempotent and which are not.

## Error Responses

- Use structured error responses with: code, message, and optional details/field violations.
- Map errors consistently across all endpoints (same error → same response structure).
- Include enough context for the client to fix the request without guessing.
- Do not leak internal details (class names, stack traces, SQL errors) in error responses.

## Versioning

- Use package-level versioning for gRPC (`package service.v1;`).
- Use path-based versioning for REST (`/api/v1/resources`).
- Never reuse removed field numbers in protobuf — use `reserved`.
- Deprecate before removing; communicate timelines to consumers.

## Naming Conventions

- **gRPC RPCs**: `VerbNoun` (e.g., `UploadFile`, `GetFileMetadata`, `ListFiles`).
- **gRPC messages**: `NounVerb{Request|Response}` (e.g., `UploadFileRequest`).
- **REST endpoints**: Plural nouns for resources (`/files`, `/users`), verbs via HTTP methods.
- **Fields**: `snake_case` in proto, consistent casing in REST (camelCase or snake_case — pick one).

## Streaming & Large Payloads

- Use streaming for file transfers and large result sets.
- Define a consistent chunk size in configuration (not hardcoded).
- Send metadata in the first message, data in subsequent messages.
- Support cancellation and partial results where appropriate.
- Document maximum payload sizes and enforce them server-side.
