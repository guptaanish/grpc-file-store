# Documentation Rules

## Markdown Files

- Keep documentation concise and up to date with the codebase.
- Use proper heading hierarchy (`#` → `##` → `###`); do not skip levels.
- Use tables for structured data (API endpoints, configuration properties, technology stacks).
- Use code blocks with language identifiers for code snippets (e.g., ` ```java`, ` ```bash`).
- End files with a single newline.

## Documentation Sync

- After **every** code change, review and update **all** `*.md` files to reflect the current state.
- **MANDATORY**: Verify that **no stale references** exist to classes, packages, methods, files, or features that have been deleted, moved, or renamed.
- Use project-wide search to identify and update all references when moving or renaming code components.

## Diagrams in Markdown

- **Always use Mermaid** for diagrams in `.md` files wherever possible — architecture, flow, sequence, class, ER, state, and dependency diagrams.
- Prefer Mermaid over any other diagramming approach (ASCII art, external images, draw.io links).
- Do **not** use ASCII box-drawing characters (`┌`, `─`, `│`, `└`, `▼`, etc.) for architectural or flow diagrams.
- File/directory tree listings may remain as indented code blocks (these are not diagrams).
- Prefer `graph TD` (top-down) for layered architectures and `graph LR` (left-right) for request flows.
- Use `subgraph` to group related components (e.g., interceptors, service layer, data layer).
- Use `sequenceDiagram` for request/response interactions between systems.
- Use `stateDiagram-v2` for state machines (e.g., circuit breaker states, upload session lifecycle).
- Keep Mermaid diagrams readable: avoid more than 15 nodes in a single diagram; split into multiple if needed.

## README Structure

- Every project and significant sub-project should have a `README.md`.
- Include at minimum: purpose, prerequisites, build/run instructions, and architecture overview.
- Use Mermaid for the architecture overview diagram.
- Include a table of available scripts/commands for quick reference.
