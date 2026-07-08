# Javadoc Rules

## Requirements

- **All** Java classes, member variables, methods, and static variables **must** have a Javadoc comment.
- All method Javadocs **must** include:
  - `@param` tag for every parameter.
  - `@return` tag for every non-void method.
  - `@throws` tag for every checked exception declared in the method signature.
- Private methods may use concise single-line Javadoc but must still include `@param`/`@return`/`@throws` where applicable.
- Field Javadocs must use multi-line format:
  ```java
  /**
   * Description of the field.
   */
  ```
- All Javadoc descriptions must end with a period (`.`).

## Documentation Style

- Write **concise** Javadoc comments on all public classes, interfaces, and methods.
- Include `@param`, `@return`, and `@throws` tags where applicable.
- Add example code snippets in Javadoc when they help clarify usage.
- Write **Javadoc** comments on all class-level fields (both static and non-static), describing their purpose.
- Always use **multi-line** Javadoc format for field comments — the opening `/**`, the description line(s), and the closing `*/` must each be on separate lines. Do **not** use single-line `/** ... */` style.
- Include clear **inline comments** within methods where the logic is not immediately obvious.

## Verbosity

- **All documentation text must end with a period (`.`)** — This applies to Javadoc descriptions, `@param`, `@return`, `@throws` tags, inline comments, and field Javadoc.
- **Avoid overly verbose documentation** — Describe *what* and *why*, not *how* (the code shows how).
- **Exceptions for complex logic** — Detailed documentation is appropriate for concurrency logic, complex algorithms, public APIs with subtle contracts, and security-sensitive code.
- **Default to brevity** — If the method name and signature are self-explanatory, a one-line summary is sufficient.

## Code Style

- **All class-level member variables** must have:
  - **Javadoc comments** describing their purpose (use multi-line format) — not required for test class variables with self-explanatory names.
  - One **blank line** after each variable declaration to visually separate fields.
- **All methods** must have:
  - **Javadoc comments** describing their purpose, parameters, return values, and exceptions (not required for test methods with descriptive names).
  - One **blank line** after each method to visually separate methods.
