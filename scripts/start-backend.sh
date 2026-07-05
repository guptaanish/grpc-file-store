#!/usr/bin/env bash
# Start the Spring Boot gRPC backend
set -e

cd "$(dirname "$0")/../backend"
echo "Starting backend on gRPC:9090 and REST:8080..."
./gradlew bootRun
