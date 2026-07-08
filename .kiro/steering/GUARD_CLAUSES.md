# Guard Clause Rules

## Early-Return Pattern

- **Prefer early returns** — Validate preconditions at the top of a method and return/throw immediately on failure. Avoid wrapping the entire method body in an `if` block.
- **Reduce nesting** — Each guard clause eliminates one level of indentation for the happy path.
- **Order guards logically** — Check null/empty first, then format validation, then business rules.
- **No else after return** — If a branch returns or throws, do not use `else` for the subsequent code.

## Examples

```java
// BAD — nested happy path
public void process(String id) {
    if (id != null && !id.isBlank()) {
        var entity = repository.findById(id);
        if (entity.isPresent()) {
            // ... deep logic
        } else {
            throw new NotFoundException("Not found");
        }
    } else {
        throw new IllegalArgumentException("ID required");
    }
}

// GOOD — guard clauses with early return
public void process(String id) {
    if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("ID required");
    }
    var entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Not found"));
    // ... flat logic at base indentation
}
```

## Rules

- Methods should not have their entire body wrapped inside a single `if` condition. Invert the condition and return/throw early.
- Maximum nesting depth of happy-path logic should be **2 levels** (not counting try/finally or synchronized blocks).
- Guard clauses must be at the **top** of the method before any business logic.
- Use `Optional.orElseThrow()` instead of `if (opt.isEmpty()) { throw ... }` followed by `opt.get()` when the sole purpose is to unwrap or fail.