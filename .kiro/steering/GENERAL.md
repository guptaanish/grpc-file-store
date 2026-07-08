# General Rules

## Kiro Skills Awareness

- The `~/.kiro/skills/` directory contains task-specific workflow procedures (skills) in subdirectories (`~/.kiro/skills/<name>/SKILL.md`).
- Skills are loaded on demand by the default agent via `skill://.kiro/skills/**/SKILL.md`.
- Reference skills when the user's request matches a skill workflow (e.g., code review, new endpoint, refactoring, troubleshooting).
- When asked about available skills or workflows, read `~/.kiro/skills/` directory contents.

## Kiro Steering Rules Awareness

- The `~/.kiro/steering/` directory contains mandatory coding rules and guidelines that govern all development work.
- These steering rules include architecture principles, coding standards, Spring Boot conventions, security guidelines, testing requirements, and documentation standards.
- All code changes and development decisions must comply with the established steering rules.
- When working on any project, always reference and follow the applicable steering rules from `~/.kiro/steering/` (e.g., ARCHITECTURE.md, SPRING_BOOT.md, CODE_STYLE.md, SECURITY.md, TESTING.md).
