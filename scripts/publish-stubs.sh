#!/usr/bin/env bash
# Publish the gRPC stubs artifact (com.example:grpc-file-store-stubs) to the local
# Maven repository (~/.m2), so other JVM services can depend on it for
# service-to-service gRPC integration.
#
# To publish to a shared repository instead, point the `publishing` block in
# backend/stubs/build.gradle.kts at your Nexus/Artifactory/GitHub Packages repo.
set -e

cd "$(dirname "$0")/../backend"

echo "Publishing grpc-file-store-stubs to the local Maven repository (~/.m2)..."
./gradlew :stubs:publishToMavenLocal

echo ""
echo "✅  Published. Consume it from another JVM service with:"
echo "      implementation(\"com.example:grpc-file-store-stubs:0.0.1-SNAPSHOT\")"
