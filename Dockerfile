# ─── Stage 1: Build Frontend ──────────────────────────────────────────────────
FROM node:22-alpine AS frontend-build

WORKDIR /app/frontend
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/.npmrc ./
RUN corepack enable && corepack prepare pnpm@9.15.4 --activate && pnpm install --frozen-lockfile

COPY frontend/ ./
COPY buf.yaml buf.gen.yaml /app/
COPY backend/stubs/src/main/proto /app/backend/stubs/src/main/proto
RUN cd /app && npx --prefix frontend buf generate
RUN pnpm build

# ─── Stage 2: Build Backend ───────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app/backend
COPY backend/gradle/ gradle/
COPY backend/gradlew backend/build.gradle.kts backend/settings.gradle.kts backend/gradle.properties ./
# Stubs subproject (build script + proto) — required to configure the multi-module build
COPY backend/stubs/ stubs/
RUN ./gradlew dependencies --no-daemon || true

COPY backend/src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ─── Stage 3: Production Image ────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the Spring Boot fat JAR
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar

# Copy the frontend build output into Spring Boot's static resources directory
# Spring Boot serves files from /app/static/ automatically
COPY --from=frontend-build /app/frontend/dist/ /app/static/

# Create storage directory
RUN mkdir -p /app/file-store-data && chown -R app:app /app

USER app

EXPOSE 8080 9090

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
