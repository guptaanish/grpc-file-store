# Containerize

Workflow for containerizing a Java application with Docker, optimized for production deployment.

## Steps

1. **Assess application requirements** — Determine:
   - Java version and base image needs (Eclipse Temurin, Amazon Corretto, Distroless)
   - Required system dependencies (fonts, native libs, etc.)
   - Exposed ports (HTTP, gRPC, management)
   - Volume mounts needed (file storage, logs, temp)
   - Environment-specific configuration approach (env vars, config maps, secrets)

2. **Write multi-stage Dockerfile** — Create an optimized Dockerfile:
   - **Build stage**: Use full JDK image, copy source, run Gradle/Maven build
   - **Runtime stage**: Use minimal JRE image (distroless or alpine-based)
   - Leverage Docker layer caching (copy dependency files first, then source)
   - Set appropriate JVM flags (`-XX:+UseContainerSupport`, memory limits)
   - Run as non-root user
   - Use `.dockerignore` to exclude build artifacts, IDE files, `.git`

3. **Configure JVM for containers** — Ensure proper resource awareness:
   - Use `-XX:MaxRAMPercentage=75.0` instead of fixed `-Xmx`
   - Enable container-aware GC (`-XX:+UseContainerSupport` — default since Java 10)
   - Set appropriate GC algorithm (G1GC for general, ZGC for low-latency)
   - Configure thread pools to respect container CPU limits

4. **Add health checks** — Configure container health monitoring:
   - `HEALTHCHECK` instruction in Dockerfile or orchestrator-level probes
   - Liveness probe: basic process alive check
   - Readiness probe: application ready to serve traffic (actuator health, gRPC health)
   - Startup probe: for slow-starting applications

5. **Create Docker Compose for local development** — Provide a `docker-compose.yml`:
   - Application service with proper port mapping
   - Dependencies (database, cache, message broker) as services
   - Volume mounts for local development convenience
   - Environment variables for local configuration

6. **Optimize image size and security** — Apply best practices:
   - Use specific image tags (not `latest`)
   - Minimize layers, combine RUN commands where logical
   - Remove build tools from runtime image
   - Scan image for vulnerabilities (`docker scout`, `trivy`, `grype`)
   - Set `USER` to non-root, drop capabilities

7. **Configure for orchestration (optional)** — If deploying to Kubernetes:
   - Create deployment manifest with resource requests/limits
   - Configure horizontal pod autoscaler (HPA)
   - Set pod disruption budgets for availability
   - Use ConfigMaps for configuration, Secrets for sensitive data

8. **Test the container** — Verify:
   - Application starts and passes health checks
   - Graceful shutdown works (SIGTERM handling)
   - Logs go to stdout/stderr (not files)
   - Resource limits are respected
   - Integration tests pass against containerized app
