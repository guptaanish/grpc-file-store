# REST API Design Rules

## Conventions

- **Consistent Endpoints** — Use RESTful conventions: `GET /resources`, `POST /resources`, `GET /resources/{id}`, `DELETE /resources/{id}`.
- **HTTP Status Codes** — Use appropriate codes: `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`, `500 Internal Server Error`.
- **Request/Response DTOs** — Use separate DTOs for requests and responses; don't expose domain models directly.
- **Validation** — Use `@Valid` on request bodies; return `400` with validation errors.
- **Error Responses** — Return consistent error response structure with timestamp, status, message, and path.
