# gRPC File Store

A file storage service with a gRPC backend and a web-based UI.

## Project Structure

```
grpc-file-store/
├── backend/    # Spring Boot gRPC service (Java 21, Gradle)
├── frontend/   # Web UI (coming soon)
└── .kiro/      # Shared AI steering rules and skills
```

## Backend

The gRPC file storage service built with Spring Boot and Java 21.

```bash
cd backend
./gradlew build
./gradlew bootRun
```

See [backend/README.md](backend/README.md) for full documentation.

## Frontend

The web UI for the file store service (under development).
