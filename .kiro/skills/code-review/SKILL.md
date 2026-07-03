---
name: code-review
description: Review a pull request or code change for correctness, security, performance, and style.
---

# Code Review

Review a pull request or code change for correctness, security, performance, and style.

## Procedure

1. Read the diff (all changed files).
2. For each file, check:
   - **Correctness**: Logic errors, edge cases, null handling, resource leaks.
   - **Security**: Input validation, path traversal, injection, secrets exposure.
   - **Performance**: Unnecessary allocations, N+1 queries, blocking calls, missing caching.
   - **Concurrency**: Thread safety, race conditions, proper synchronization.
   - **Architecture**: SRP violations, coupling, missing abstractions.
   - **Tests**: New code has tests, edge cases covered, no test logic.
   - **Documentation**: Javadoc matches signatures, README/PROJECT.md updated if needed.
   - **Style**: Matches project conventions (final locals, var usage, Lombok, records).
3. Present findings organized file by file using this format:

   ## File: `path/to/file.ext`

   | Severity | Line | Comment |
   |----------|------|---------|
   | Critical | 42   | Comment text here |
   | Major    | 15   | Comment text here |
   | Minor    | 78   | Comment text here |
   | Suggestion | 5  | Comment text here |

4. Summarize: total issues by severity, overall assessment (approve / request changes).

## Severity Definitions

- **Critical**: Bugs, security vulnerabilities, data loss risks. Must fix before merge.
- **Major**: Significant design issues, missing error handling, test gaps. Should fix.
- **Minor**: Style inconsistencies, naming, minor improvements. Fix if easy.
- **Suggestion**: Optional improvements, alternative approaches. Author's discretion.

## Rules

- Do NOT post comments on the PR unless explicitly asked.
- Focus on what changed — don't review unchanged code unless it's affected by the change.
- Praise good patterns briefly; spend most effort on issues.
- If no issues found, say so clearly and approve.
