# Concurrency & Thread Management Rules

## Framework-Managed Concurrency

- Zero manual Thread creation or ExecutorService management within `@Service` classes.
- Use `@Async` for fire-and-forget tasks.
- Rely on Spring's underlying container or Project Loom's virtual threads.

## Protect Data, Not Code (JCIP)

- Concurrency is about managing access to shared, mutable state.
- If data is thread-confined or immutable, you don't need synchronization.

## Atomicity vs. Visibility

- **Atomicity** — Use AtomicInteger or similar for single units of operation.
- **Visibility** — Ensure changes are seen across threads via the volatile keyword or "Happens-Before" guarantees.
- **Safe Publication** — Use ConcurrentHashMap or volatile references to ensure a thread sees a fully initialized state of an object.
- **Avoid Excessive Synchronization** — Never call an "alien" method from within a synchronized block to avoid deadlocks.

## Thread-Free Logic Goal

By utilizing Spring's Declarative Programming model, treat the application as a synchronous flow of data. The framework handles the "plumbing"—transaction boundaries, security contexts, and thread pooling. Combined with Effective Java design and JCIP data safety, this produces high-performance, testable, and thread-safe Java code.
