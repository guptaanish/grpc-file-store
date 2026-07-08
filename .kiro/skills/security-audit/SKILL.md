# Security Audit

Workflow for reviewing a Java application for security vulnerabilities, misconfigurations, and compliance gaps.

## Steps

1. **Dependency vulnerability scan** — Check for known CVEs in project dependencies:
   - Run `./gradlew dependencyCheckAnalyze` (OWASP Dependency-Check plugin) or equivalent
   - Review Dependabot/Snyk/Renovate alerts if configured
   - Flag any HIGH or CRITICAL vulnerabilities
   - Recommend upgrades or mitigations for each finding

2. **Input validation review** — Verify all user inputs are validated:
   - API request bodies use `@Valid`/`@Validated` with Jakarta Bean Validation
   - Path parameters and query params are bounds-checked
   - File uploads enforce size limits and content-type validation
   - No raw user input reaches SQL, shell commands, or log statements
   - gRPC request fields are validated before processing

3. **Authentication & authorization check** — Review access controls:
   - Endpoints require appropriate authentication
   - Authorization checks enforce least privilege
   - No privilege escalation paths exist
   - Tokens/sessions have appropriate expiry

4. **Injection prevention** — Check for injection vectors:
   - SQL: parameterized queries or JPA (no string concatenation)
   - Path traversal: file paths validated, `..` rejected, canonicalized
   - Log injection: user input sanitized before logging (CRLF stripped)
   - Command injection: no `Runtime.exec()` with user input
   - Deserialization: no unsafe deserialization of untrusted data

5. **Sensitive data handling** — Verify:
   - No secrets in source code or config files committed to VCS
   - Passwords/tokens not logged (even at DEBUG level)
   - PII masked in logs and error responses
   - HTTPS/TLS configured for production
   - Sensitive headers not exposed in error responses

6. **Error handling review** — Ensure:
   - Stack traces not leaked to clients in production
   - Error responses don't reveal internal implementation details
   - Global exception handler returns consistent, safe error format
   - Failed auth attempts are logged for monitoring

7. **Configuration security** — Check:
   - Debug endpoints disabled in production (H2 console, actuator endpoints restricted)
   - CORS configured restrictively
   - Security headers present (CSP, X-Frame-Options, etc. for HTTP APIs)
   - Default credentials changed or removed

8. **Produce findings report** — Summarize findings by severity (CRITICAL, HIGH, MEDIUM, LOW) with specific file locations, remediation steps, and priority order.
