# Performance Test

Workflow for load testing, profiling, and identifying performance bottlenecks in a Java application.

## Steps

1. **Identify target endpoints/methods** — Determine which APIs or code paths to benchmark based on user request or critical-path analysis.

2. **Establish baseline metrics** — Run the application and capture current response times, throughput, memory usage, and CPU utilization under normal load.

3. **Design load test scenarios** — Create realistic test scenarios:
   - Ramp-up load (gradual increase in concurrent users)
   - Sustained load (steady-state at expected peak)
   - Spike test (sudden burst of traffic)
   - Soak test (extended duration for memory leak detection)

4. **Write or configure load tests** — Use appropriate tools for the project:
   - **JMH** for microbenchmarks (method-level)
   - **Gatling** or **k6** for HTTP/gRPC load testing
   - **ghz** for gRPC-specific benchmarks
   - Ensure tests are repeatable and parameterized

5. **Execute tests and collect results** — Run load tests, capturing:
   - p50, p95, p99 latency
   - Requests per second (throughput)
   - Error rate under load
   - Resource utilization (CPU, memory, threads, GC pauses)

6. **Profile and identify bottlenecks** — Use profiling tools:
   - **async-profiler** for CPU/allocation flame graphs
   - **JFR (Java Flight Recorder)** for production-safe profiling
   - **VisualVM** or **JConsole** for heap analysis
   - Check for thread contention, excessive GC, N+1 queries, connection pool exhaustion

7. **Recommend or implement optimizations** — Based on findings:
   - Caching, connection pooling, query optimization
   - Async processing, batch operations
   - JVM tuning (heap size, GC algorithm)
   - Code-level fixes (algorithm complexity, object allocation reduction)

8. **Re-test and validate improvements** — Run the same benchmarks after changes to confirm improvement and ensure no regressions.

9. **Document results** — Record baseline vs. optimized metrics, configuration used, and any trade-offs introduced.
