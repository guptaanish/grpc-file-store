# Dependency Injection Rules

## Constructor Injection

- **Constructor Injection** — Always use constructor injection; avoid field injection (`@Autowired` on fields).
- **Final Fields** — Make injected dependencies `final` to ensure immutability.
- **Single Constructor** — If a class has only one constructor, `@Autowired` is optional (implicit).
- **Avoid `@Autowired` on Setters** — Setter injection makes dependencies optional, which is rarely desired.
