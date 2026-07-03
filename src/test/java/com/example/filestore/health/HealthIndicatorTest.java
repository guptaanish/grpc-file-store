package com.example.filestore.health;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Status;

import com.example.filestore.config.FileStoreProperties;
import com.example.filestore.config.StorageProperties;
import com.example.filestore.config.StorageProperties.LocalStorageProperties;
import com.example.filestore.config.StorageType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthIndicatorTest {

    @TempDir
    Path tempDir;

    @Test
    void storageHealthShouldBeUpWhenDirectoryWritable() {
        final var props = new FileStoreProperties(65536, 104857600,
                new StorageProperties(StorageType.LOCAL, new LocalStorageProperties(tempDir.toString())), null, null);
        final var indicator = new StorageHealthIndicator(props);

        final var health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void storageHealthShouldBeDownWhenDirectoryMissing() {
        final var props = new FileStoreProperties(65536, 104857600,
                new StorageProperties(StorageType.LOCAL, new LocalStorageProperties("/nonexistent/dir/xyz")), null, null);
        final var indicator = new StorageHealthIndicator(props);

        final var health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void databaseHealthShouldBeUpWhenConnected() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        final var indicator = new DatabaseHealthIndicator(dataSource);
        final var health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void databaseHealthShouldBeDownWhenConnectionFails() throws SQLException {
        final var dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        final var indicator = new DatabaseHealthIndicator(dataSource);
        final var health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
