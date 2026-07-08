# Security Rules

## Input Validation

- **Validate All Inputs** — Never trust user input; validate at API boundaries.
- **Sanitize Logs** — Strip CRLF and other injection characters before logging user input.
- **Path Traversal Prevention** — Validate file paths; reject `..`, absolute paths, and path separators in user input.
- **Size Limits** — Enforce maximum sizes for file uploads, request bodies, and string inputs.
- **Fail-Fast Validation** — Validate at the entry point using jakarta.validation or Assert statements.
- Use `@Valid` and `@Validated` at the API layer.

## Sensitive Data

- **No Secrets in Code** — Never hardcode passwords, API keys, or tokens; use environment variables or secret management.
- **No Secrets in Logs** — Avoid logging sensitive data (passwords, tokens, PII); mask if necessary.
- **Secure Defaults** — Default to secure configurations; require explicit opt-in for insecure options.
