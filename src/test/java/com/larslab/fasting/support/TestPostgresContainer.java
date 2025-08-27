package com.larslab.fasting.support;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton holder for a PostgreSQL Testcontainers instance reused across all
 * integration tests within the same JVM fork. Avoids per-class startup cost.
 */
public final class TestPostgresContainer {
    public static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    static {
        INSTANCE.start();
    }

    private TestPostgresContainer() {}
}
