#!/usr/bin/env bash
# Start the frontend Vite dev server
set -e

cd "$(dirname "$0")/../frontend"

if [ ! -d "node_modules" ]; then
  echo "Installing dependencies..."
  pnpm install
fi

echo "Starting frontend on http://localhost:5173..."
pnpm dev
