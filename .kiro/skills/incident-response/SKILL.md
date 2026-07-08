# Incident Response

Workflow for diagnosing and resolving production issues in a Java application.

## Steps

1. **Gather symptoms** — Collect initial information:
   - What is the user-visible impact? (errors, latency, downtime)
   - When did it start? (correlate with recent deployments, config changes, traffic spikes)
   - What is the blast radius? (all users, specific endpoints, specific regions)
   - Are there any error messages or alerts?

2. **Check health and metrics** — Review application observability:
   - Health endpoints (`/actuator/health`, gRPC health checks)
   - Metrics dashboards (request rate, error rate, latency percentiles)
   - Resource utilization (CPU, memory, disk, network, thread count)
   - JVM metrics (GC pauses, heap usage, thread states)
   - Database connection pool status

3. **Review logs** — Examine application logs:
   - Filter by timestamp window of incident
   - Look for ERROR/WARN level entries
   - Check for stack traces, OOM errors, connection timeouts
   - Correlate request IDs across services
   - Check for log volume anomalies (sudden silence or flood)

4. **Identify root cause category** — Narrow down the issue type:
   - **Resource exhaustion** — Memory leak, thread pool saturation, connection pool exhaustion, disk full
   - **External dependency failure** — Database down, third-party API timeout, DNS resolution failure
   - **Code bug** — NPE, infinite loop, deadlock, race condition
   - **Configuration error** — Wrong endpoint, expired credentials, incorrect limits
   - **Traffic/load** — Unexpected spike, DDoS, missing rate limiting

5. **Apply immediate mitigation** — Stabilize first, fix root cause later:
   - Restart affected instances (if stateless)
   - Scale up/out if resource-constrained
   - Enable circuit breakers or fallbacks
   - Roll back recent deployment if correlated
   - Block abusive traffic if applicable

6. **Diagnose root cause** — Deep investigation:
   - Take thread dumps for deadlock/contention analysis
   - Capture heap dumps for memory leak analysis
   - Review recent code changes (git log, PR history)
   - Reproduce in a non-production environment if possible
   - Check for known issues in dependencies

7. **Implement fix** — Apply the permanent resolution:
   - Write a targeted fix addressing root cause
   - Add regression tests covering the failure scenario
   - Add monitoring/alerting to detect recurrence early
   - Review fix for unintended side effects

8. **Post-incident review** — Document lessons learned:
   - Timeline of events (detection → mitigation → resolution)
   - Root cause and contributing factors
   - What went well and what could improve
   - Action items (monitoring gaps, runbook updates, architectural improvements)
