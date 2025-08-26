package com.larslab.fasting.support;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Base class providing a single shared PostgreSQL Testcontainers instance and core
 * datasource / Flyway dynamic properties for all integration tests. Subclasses may
 * declare additional {@link DynamicPropertySource} methods to register extra
 * properties (e.g. feature flags, rate limiting config) without repeating the
 * container boilerplate.
 */
@AutoConfigureTestDatabase(replace = NONE)
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", TestPostgresContainer.INSTANCE::getJdbcUrl);
    registry.add("spring.datasource.username", TestPostgresContainer.INSTANCE::getUsername);
    registry.add("spring.datasource.password", TestPostgresContainer.INSTANCE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
