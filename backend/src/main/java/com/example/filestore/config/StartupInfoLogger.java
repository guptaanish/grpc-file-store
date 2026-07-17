package com.example.filestore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs a startup banner once the application is ready, advertising the key
 * service endpoints — including the generated gRPC API documentation URL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupInfoLogger {

    /**
     * Default HTTP/REST port used when {@code server.port} is not configured.
     */
    private static final String DEFAULT_HTTP_PORT = "8080";

    /**
     * Default gRPC port used when {@code grpc.server.port} is not configured.
     */
    private static final String DEFAULT_GRPC_PORT = "9090";

    /**
     * Spring environment used to resolve the configured server and gRPC ports.
     */
    private final Environment environment;

    /**
     * Logs the endpoint banner after the application context is fully initialized.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        var httpPort = environment.getProperty("server.port", DEFAULT_HTTP_PORT);
        var grpcPort = environment.getProperty("grpc.server.port", DEFAULT_GRPC_PORT);
        var reflectionEnabled =
                environment.getProperty("grpc.server.reflection-service-enabled", Boolean.class, false);

        // The HTML API reference is generated from the proto (buf + protoc-gen-doc)
        // and served from Spring Boot's static resources at /grpc-docs.html.
        var docsUrl = "http://localhost:" + httpPort + "/grpc-docs.html";
        var swaggerUrl = "http://localhost:" + httpPort + "/swagger-ui.html";

        log.info(
                """

                        ─────────────────────────────────────────────────────────────
                          gRPC File Store — ready
                        ─────────────────────────────────────────────────────────────
                          gRPC server:    localhost:{}
                          REST API:       http://localhost:{}/api/v1/files
                          Swagger UI:     {}
                          Health:         http://localhost:{}/actuator/health
                          gRPC API docs:  {}
                        ─────────────────────────────────────────────────────────────""",
                grpcPort,
                httpPort,
                swaggerUrl,
                httpPort,
                docsUrl);

        if (reflectionEnabled) {
            log.info(
                    "gRPC reflection is enabled — run ./scripts/grpc-ui.sh for an interactive UI at http://localhost:8082.");
        }
    }
}
