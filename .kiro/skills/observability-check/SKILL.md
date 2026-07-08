---
name: observability-check
description: Verify that metrics, dashboards, and observability infrastructure match the current codebase.
---

# Observability Check

Verify that metrics, dashboards, and observability infrastructure match the current codebase.

## Procedure

1. **Custom metrics audit**: Find all `Counter`, `Timer`, `Gauge` registrations in Java code. Verify each metric name is:
   - Used in at least one Grafana dashboard JSON (`observability/grafana/provisioning/dashboards/json/*.json`).
   - Scraped by Prometheus (check `observability/prometheus/prometheus.yml` targets).
2. **Dashboard validity**: For each Grafana dashboard JSON, verify:
   - All referenced metric names exist in the codebase.
   - No dashboard references a metric that was removed or renamed.
3. **OTel Collector config**: Verify `otel-collector-config.yml`:
   - Receivers match what the app exports (OTLP traces, filelog for logs).
   - Exporters point to running services (Loki, Tempo).
   - Log file path matches what Logback writes to.
4. **Actuator endpoints**: Verify `management.endpoints.web.exposure.include` in `application.yml` matches what's documented in README.md.
5. **Docker Compose**: Verify all observability services (otel-collector, loki, tempo, prometheus, grafana) have correct image versions matching PROJECT.md technology stack.
6. **Report findings**: List any orphaned metrics, broken dashboard references, or version mismatches.

## Rules

- Every custom metric registered in code should appear in at least one dashboard.
- Dashboard metric names must exactly match what the code emits.
- Image versions in docker-compose.yml must match PROJECT.md technology stack table.
