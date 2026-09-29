package com.expense.tracker;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base for integration tests that need a real PostgreSQL. One container is shared by every
 * subclass in the JVM (singleton pattern) so cached Spring contexts keep a live database.
 * The schema comes from the Flyway migrations, and Hibernate only validates it.
 */
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
public abstract class AbstractPostgresIT {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }
}
