# Code Comments & Javadoc Rules

## Javadoc Requirements

- **All** Java classes, interfaces, member variables, methods, and static variables **must** have a Javadoc comment.
- All method Javadocs **must** include:
  - `@param` tag for every parameter.
  - `@return` tag for every non-void method.
  - `@throws` tag for every checked exception declared in the method signature.
- Private methods may use concise single-line Javadoc but must still include `@param`/`@return`/`@throws` where applicable.
- Test methods with descriptive names do not require Javadoc.

## Javadoc Format

- All Javadoc descriptions must end with a period (`.`).
- Always use **multi-line** Javadoc format for field comments:
  ```java
  /**
   * Description of the field.
   */
  ```
- Do **not** use single-line `/** ... */` style for fields.
- Add example code snippets in Javadoc when they help clarify usage.

## Javadoc Style

- Write **concise** Javadoc — describe *what* and *why*, not *how* (the code shows how).
- If the method name and signature are self-explanatory, a one-line summary is sufficient.
- Detailed documentation is appropriate for:
  - Concurrency logic.
  - Complex algorithms.
  - Public APIs with subtle contracts.
  - Security-sensitive code.

## Inline Comments

- Include clear inline comments within methods where the logic is not immediately obvious.
- All inline comments must end with a period (`.`).
- Prefer explaining *why* over *what* — the code already shows what it does.
- Do **not** use commented-out code; delete it (version control preserves history).
- Use `// TODO:` for planned improvements (include a brief description of what and why).

## Field & Method Spacing

- **All class-level member variables** must have:
  - A Javadoc comment describing their purpose (multi-line format) — not required for test class variables with self-explanatory names.
  - One **blank line** after each variable declaration to visually separate fields.
- **All methods** must have:
  - A Javadoc comment describing their purpose, parameters, return values, and exceptions — not required for test methods with descriptive names.
  - One **blank line** after each method to visually separate methods.
