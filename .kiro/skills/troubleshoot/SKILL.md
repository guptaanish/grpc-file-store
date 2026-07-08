---
name: troubleshoot
description: Diagnose and resolve issues with the Mock Discover environment.
---

# Troubleshoot

Diagnose and resolve issues with the Mock Discover environment.

## Procedure

1. **Check Docker services**: Run `docker compose ps` — all services should be "Up".
2. **Check application health**: `curl http://localhost:4000/actuator/health` — should return `{"status":"UP"}`.
3. **Check S3 connectivity**: `curl http://localhost:4566/_localstack/health` — S3 service should be "running".
4. **Check nginx proxy**: `curl -I http://localhost:4566` — should return 200 or proxy response.
5. **Check application logs**: `docker compose logs app --tail=50` — look for errors or stack traces.
6. **Check OTel Collector**: `docker compose logs otel-collector --tail=20` — verify no export errors.
7. **Check Grafana**: `curl http://localhost:3001/api/health` — should return "ok".
8. **Check GraphQL**: `curl -X POST http://localhost:4000/graphql -H "Content-Type: application/json" -d '{"query":"{ cases(filter:\"\") { id name } }"}'` — should return data.
9. **Common issues**:
   - Port conflict: Check if 4000, 4566, or 3001 are already in use.
   - Colima not running: Run `colima start`.
   - Sync dir not writable: Check `mock-discover.s3.sync-dir` in application.yml.
   - OTel agent crash: Check `docker compose logs app` for agent errors.
10. **Report**: Summarize which checks passed/failed and suggest fixes.

## Rules

- Always check Docker services first — most issues stem from containers not running.
- Check logs before guessing — the error message usually points to the root cause.
- If a service won't start, check its dependencies in docker-compose.yml.
