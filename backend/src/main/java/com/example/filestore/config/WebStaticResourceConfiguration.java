package com.example.filestore.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Configuration for serving the React SPA frontend from Spring Boot.
 *
 * <p>In production, the frontend build output is placed in {@code /app/static/}.
 * This configurer serves static assets and falls back to {@code index.html} for SPA routes.
 */
@Configuration
public class WebStaticResourceConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("file:./static/")
                .resourceChain(true)
                .addResolver(new SpaFallbackResourceResolver());
    }

    /**
     * Custom resource resolver that serves static files if they exist,
     * and falls back to index.html for SPA client-side routes.
     */
    private static class SpaFallbackResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // Never intercept API, actuator, H2 console, or Swagger/OpenAPI paths
            if (resourcePath.startsWith("api/")
                    || resourcePath.startsWith("actuator/")
                    || resourcePath.startsWith("h2-console")
                    || resourcePath.startsWith("swagger-ui")
                    || resourcePath.startsWith("v3/api-docs")
                    || resourcePath.startsWith("webjars/")) {
                return null;
            }

            // Try to resolve the actual resource
            Resource resource = location.createRelative(resourcePath);
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            // For SPA routes, fall back to index.html
            Resource indexHtml = new FileSystemResource("./static/index.html");
            if (indexHtml.exists() && indexHtml.isReadable()) {
                return indexHtml;
            }

            return null;
        }
    }
}
