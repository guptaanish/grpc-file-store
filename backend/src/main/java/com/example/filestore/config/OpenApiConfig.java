package com.example.filestore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) metadata for the REST API surface.
 *
 * <p>Only the HTTP endpoints (upload/download) consumed by the browser UI are described here.
 * The gRPC contract for service-to-service integration is documented separately as a static
 * proto reference at {@code /grpc-docs.html}.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the OpenAPI document metadata surfaced in Swagger UI.
     *
     * @return the configured OpenAPI info bean.
     */
    @Bean
    public OpenAPI fileStoreOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("gRPC File Store — REST API")
                        .description(
                                "HTTP endpoints used by the browser UI for file upload and download. "
                                        + "The gRPC API (list, metadata, delete, copy, move, versions, streaming) "
                                        + "is documented separately at /grpc-docs.html.")
                        .version("v1")
                        .license(new License().name("Private — internal project")));
    }
}
