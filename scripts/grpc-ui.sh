#!/usr/bin/env bash
# Launch grpcui — an interactive, Swagger-UI-like web interface for the gRPC
# service. It uses server reflection (enabled in the dev/local profile) to
# discover services and lets you invoke RPCs from the browser.
#
# Requires the gRPC server to be running on localhost:9090.
#
# Usage: ./scripts/grpc-ui.sh [port]   (default UI port: 8082)
set -e

GRPC_TARGET="localhost:9090"
UI_PORT="${1:-8082}"

if ! command -v grpcui >/dev/null 2>&1; then
  echo "❌  grpcui is not installed. Install it with one of:"
  echo "      brew install grpcui"
  echo "      go install github.com/fullstorydev/grpcui/cmd/grpcui@latest"
  echo ""
  echo "    More info: https://github.com/fullstorydev/grpcui"
  exit 1
fi

echo "Starting grpcui → gRPC ${GRPC_TARGET}"
echo "Open: http://localhost:${UI_PORT}"
echo "(Requires the backend running with reflection enabled — dev/local profile.)"
echo ""

# -plaintext: server runs without TLS locally.
# -port: pin the UI port so it doesn't collide with 8080/8081/9090/9901/5173.
exec grpcui -plaintext -port "${UI_PORT}" "${GRPC_TARGET}"
