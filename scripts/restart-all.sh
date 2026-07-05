#!/usr/bin/env bash
# Stop and restart the full stack (backend + frontend + envoy) via Docker Compose
set -e

cd "$(dirname "$0")/.."

echo "Stopping services..."
docker compose down
echo ""
echo "Rebuilding and starting services..."
docker compose up --build -d

echo ""
echo "Waiting for services to become healthy..."
for i in $(seq 1 60); do
  STATUS=$(docker inspect --format='{{.State.Health.Status}}' grpc-file-store-app 2>/dev/null || echo "starting")
  if [ "$STATUS" = "healthy" ]; then
    break
  fi
  sleep 1
done

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                   gRPC File Store — Restarted                   ║"
echo "╠══════════════════════════════════════════════════════════════════╣"
echo "║                                                                 ║"
echo "║  Frontend (UI):        http://localhost:8080                     ║"
echo "║  REST API:             http://localhost:8080/api/v1/files        ║"
echo "║  gRPC Server:          localhost:9090                            ║"
echo "║  gRPC-Web (Envoy):     http://localhost:8081                     ║"
echo "║  Health Check:         http://localhost:8080/actuator/health     ║"
echo "║  Envoy Admin:          http://localhost:9901                     ║"
echo "║  H2 Console:           http://localhost:8080/h2-console          ║"
echo "║                                                                 ║"
echo "╠══════════════════════════════════════════════════════════════════╣"
echo "║  Services                                                       ║"
echo "╠══════════════════════════════════════════════════════════════════╣"

APP_STATUS=$(docker inspect --format='{{.State.Health.Status}}' grpc-file-store-app 2>/dev/null || echo "not running")
ENVOY_STATUS=$(docker inspect --format='{{.State.Status}}' grpc-file-store-envoy 2>/dev/null || echo "not running")

if [ "$APP_STATUS" = "healthy" ]; then
  echo "║  ✅  App (backend + frontend)    healthy                        ║"
else
  echo "║  ❌  App (backend + frontend)    $APP_STATUS                    ║"
fi

if [ "$ENVOY_STATUS" = "running" ]; then
  echo "║  ✅  Envoy (gRPC-Web proxy)      running                        ║"
else
  echo "║  ❌  Envoy (gRPC-Web proxy)      $ENVOY_STATUS                  ║"
fi

echo "║                                                                 ║"
echo "╠══════════════════════════════════════════════════════════════════╣"
echo "║  Commands                                                       ║"
echo "╠══════════════════════════════════════════════════════════════════╣"
echo "║  Stop:      docker compose down                                 ║"
echo "║  Logs:      docker compose logs -f                              ║"
echo "║  Restart:   ./scripts/restart-all.sh                            ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
