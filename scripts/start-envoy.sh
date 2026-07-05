#!/usr/bin/env bash
# Start Envoy gRPC-Web proxy via Docker (local dev mode — backend on host)
set -e

cd "$(dirname "$0")/.."
echo "Starting Envoy proxy on localhost:8081 (connecting to host backend:9090)..."
docker run --rm \
  --name grpc-file-store-envoy \
  -p 8081:8081 \
  -p 9901:9901 \
  --add-host=host.docker.internal:host-gateway \
  -v "$(pwd)/envoy/envoy-local.yaml:/etc/envoy/envoy.yaml:ro" \
  envoyproxy/envoy:v1.31-latest
