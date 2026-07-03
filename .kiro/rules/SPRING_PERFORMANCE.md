# Spring Performance Rules

## Optimization

- **Lazy Initialization** — Use `@Lazy` for expensive beans that aren't always needed.
- **Caching** — Use `@Cacheable`, `@CacheEvict`, `@CachePut` for expensive operations.
- **Async Processing** — Use `@Async` for fire-and-forget operations; configure thread pools appropriately.
- **Connection Pooling** — Configure connection pools for databases and HTTP clients.
