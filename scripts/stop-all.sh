#!/usr/bin/env bash
# Stop all running services
set -e

cd "$(dirname "$0")/.."

echo "Stopping all services..."
docker compose down

echo ""
echo "✅  All services stopped."
