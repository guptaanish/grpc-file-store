# Git & Version Control Rules

## Commit Messages

- Use **Conventional Commits** format: `<type>(<scope>): <description>`
- Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`, `ci`, `build`.
- Scope is optional but recommended (e.g., `feat(upload): add resumable upload support`).
- Description must be lowercase, imperative mood, no period at end.
- Keep the subject line under 72 characters.
- Use the body for *why* the change was made (not *what* — the diff shows that).

## Branch Naming

- Format: `<type>/<short-description>` (e.g., `feat/resumable-upload`, `fix/path-traversal`).
- Use lowercase with hyphens as separators.
- Include ticket/issue number when applicable: `feat/GFS-42-file-copy`.

## Pull Requests

- Keep PRs focused — one logical change per PR.
- Target PR size: under 400 lines changed (excluding generated code and tests).
- PR title follows commit message format: `feat(upload): add resumable upload support`.
- PR description must include: summary of changes, what was tested, any follow-up needed.
- All CI checks must pass before merging.
- Require at least one approval before merge.

## Branching Strategy

- `main` — Always deployable, protected branch.
- Feature branches — Short-lived, merged via squash or merge commit.
- Do not push directly to `main`.
- Delete branches after merging.

## What Not to Commit

- Build artifacts (`build/`, `out/`, `*.class`, `*.jar`).
- IDE files (`.idea/`, `.vscode/`, `*.iml`) — use `.gitignore`.
- Secrets, credentials, API keys — use environment variables or secret managers.
- Large binary files — use Git LFS if unavoidable.
- Generated code that can be reproduced from source (proto stubs, MapStruct impls).

## Commit Hygiene

- Each commit should compile and pass tests independently.
- Do not commit commented-out code — version control is the history.
- Separate formatting/style changes from logic changes into different commits.
- Amend or squash fixup commits before merging.
