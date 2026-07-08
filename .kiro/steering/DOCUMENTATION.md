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

## GitHub Markdown Rich Features

### Callouts / Alerts

- Use GitHub alert syntax for highlighting critical information in documentation.
- Use the appropriate severity level — do NOT overuse `CAUTION` for minor notes.
- Format: `> [!TYPE]` followed by the content on the next line(s) prefixed with `>`.
- Available types and when to use them:
  - `> [!NOTE]` — General information, context, or background. Use for "good to know" items.
  - `> [!TIP]` — Helpful advice, best practices, or shortcuts. Use for optimization hints and usage suggestions.
  - `> [!IMPORTANT]` — Key information the reader must not miss. Use for critical design constraints or prerequisites.
  - `> [!WARNING]` — Potential issues or pitfalls. Use for performance gotchas, deprecation notices, or things that may break.
  - `> [!CAUTION]` — Dangerous actions or security risks. Use for data loss risks, security vulnerabilities, or irreversible operations.
- Place callouts **immediately after** the content they relate to, not before.
- Keep callout text concise — 1–3 lines. If you need more, the content belongs in a regular section.
- Do NOT nest callouts or place them inside tables or code blocks.

### Math (LaTeX)

- Use inline math with `$expression$` for formulas within prose (e.g., complexity: $O(n \log n)$).
- Use block math with ` ```math ` fenced code blocks for standalone equations.
- Use math for: Big-O complexity, formulas for capacity planning, SLA calculations, throughput equations.
- Do NOT use math for simple numbers or text that doesn't need mathematical notation.

### Special Fenced Code Blocks

- ` ```mermaid ` — Diagrams (see Mermaid rules below).
- ` ```math ` — Block-level LaTeX equations.
- ` ```geojson ` / ` ```topojson ` — Only if documenting geographic data or location-based features.
- Standard language blocks (` ```java`, ` ```sql`, ` ```yaml`, etc.) — Always specify the language identifier.

## Diagrams in Markdown

### General Mermaid Rules

- **Always use Mermaid** for diagrams in `.md` files wherever possible — architecture, flow, sequence, class, ER, state, and dependency diagrams.
- Prefer Mermaid over any other diagramming approach (ASCII art, external images, draw.io links).
- Do **not** use ASCII box-drawing characters (`┌`, `─`, `│`, `└`, `▼`, etc.) for architectural or flow diagrams.
- File/directory tree listings may remain as indented code blocks (these are not diagrams).
- Keep Mermaid diagrams readable: avoid more than 15 nodes in a single diagram; split into multiple if needed.

### Mermaid Diagram Types — When to Use Each

| Diagram Type | Syntax | Use For |
|-------------|--------|---------|
| **Flowchart** | `graph TD` / `graph LR` | Architecture diagrams, decision flows, process flows, package structure |
| **Sequence Diagram** | `sequenceDiagram` | API request/response flows, service interactions, authentication flows |
| **Class Diagram** | `classDiagram` | Service layer design, interfaces, OOP hierarchies, dependency relationships |
| **ER Diagram** | `erDiagram` | Database schema, entity relationships, table structures with column types |
| **State Diagram** | `stateDiagram-v2` | State machines, lifecycle diagrams, workflow states, circuit breaker states |
| **Gantt Chart** | `gantt` | Project timelines, sprint planning, development phase schedules |
| **Pie Chart** | `pie` | Data distribution, resource allocation, category breakdowns |
| **Git Graph** | `gitGraph` | Branching strategies, merge workflows, release processes |
| **Mindmap** | `mindmap` | Feature brainstorming, scope overview, topic exploration |
| **Timeline** | `timeline` | Technology evolution, release history, migration roadmaps |
| **Quadrant Chart** | `quadrantChart` | Priority matrices (effort vs impact), risk assessment |

### Mermaid Style Conventions

- Use `graph TD` (top-down) for layered architectures and `graph LR` (left-right) for linear flows.
- Use `subgraph` to group related components (e.g., interceptors, service layer, data layer).
- Use descriptive node labels with `["Label Text"]` — avoid single-character node IDs without labels.
- Use `[("Label")]` for database/cylinder shapes, `["Label"]` for rectangles, `(["Label"])` for rounded.
- Add `<br/>` for multi-line labels in nodes.
- Use `-->|label|` for labeled edges to clarify relationships.
- For `sequenceDiagram`: use `participant` aliases for long service names; use `Note over` for context.
- For `erDiagram`: include column types, PK/FK markers, and constraint comments in quotes.
- For `classDiagram`: mark interfaces with `<<interface>>`, show visibility (`+`/`-`/`#`).
- For `stateDiagram-v2`: use `note right of` to annotate key states; show transitions with labels.
- For `gantt`: use `after` for dependencies between tasks; group with `section`.
- For `gitGraph`: keep branches short and merge back promptly to show realistic workflows.

## README Structure

- Every project and significant sub-project should have a `README.md`.
- Include at minimum: purpose, prerequisites, build/run instructions, and architecture overview.
- Use Mermaid for the architecture overview diagram.
- Include a table of available scripts/commands for quick reference.