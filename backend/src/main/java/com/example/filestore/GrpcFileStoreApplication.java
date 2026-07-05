package com.example.filestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the gRPC File Store application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class GrpcFileStoreApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(GrpcFileStoreApplication.class, args);
    }
}
