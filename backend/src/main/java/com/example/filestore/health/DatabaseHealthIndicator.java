package com.example.filestore.health;

import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator that checks database connectivity.
 */
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    /**
     * The application datasource.
     */
    private final DataSource dataSource;

    @Override
    public Health health() {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return Health.up().withDetail("database", "H2").build();
        } catch (SQLException e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
