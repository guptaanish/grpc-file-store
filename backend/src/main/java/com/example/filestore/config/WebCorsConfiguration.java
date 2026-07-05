package com.example.filestore.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration for the REST API endpoints.
 *
 * <p>Allows the frontend development server (and configured production origins) to
 * make cross-origin requests to the backend REST endpoints.
 */
@Configuration
public class WebCorsConfiguration {

    /**
     * Creates a CORS filter scoped to API paths.
     *
     * @param properties the CORS configuration properties.
     * @return the configured CORS filter.
     */
    @Bean
    public CorsFilter corsFilter(CorsProperties properties) {
        final var config = new CorsConfiguration();
        config.setAllowedOrigins(properties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of(
                "Content-Disposition", "X-File-Version", "X-File-Checksum"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

    /**
     * CORS configuration properties read from application.yml.
     *
     * @param allowedOrigins the list of allowed origins for CORS requests.
     */
    @ConfigurationProperties(prefix = "filestore.cors")
    public record CorsProperties(
            List<String> allowedOrigins
    ) {
    }
}
