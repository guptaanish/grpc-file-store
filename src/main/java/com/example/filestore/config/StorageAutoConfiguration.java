package com.example.filestore.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.filestore.service.StorageService;
import com.example.filestore.service.impl.LocalFileStorageService;

/**
 * Auto-configuration that creates the appropriate StorageService bean based on config.
 */
@Configuration
public class StorageAutoConfiguration {

    /**
     * Creates a local filesystem storage service.
     *
     * @param properties the file store properties.
     * @return a local StorageService implementation.
     */
    @Bean
    @ConditionalOnProperty(name = "filestore.storage.type", havingValue = "LOCAL")
    public StorageService localStorageService(FileStoreProperties properties) {
        return new LocalFileStorageService(properties.storage().local());
    }
}
