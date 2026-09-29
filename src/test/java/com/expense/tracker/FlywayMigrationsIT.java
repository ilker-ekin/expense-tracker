package com.expense.tracker;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starting the full context proves all migrations apply on PostgreSQL and that the JPA entities
 * match the resulting schema (ddl-auto=validate).
 */
@SpringBootTest
class FlywayMigrationsIT extends AbstractPostgresIT {

    private static final String ADMIN_EMAIL = "admin@admin.com";

    @Autowired JdbcTemplate jdbc;

    @Test
    void allMigrationsApplySuccessfully() {
        List<String> versions = jdbc.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank", String.class);
        assertThat(versions).containsExactly("1", "2", "3", "4", "5", "6");
    }

    @Test
    void defaultAdminIsNotPresentAfterMigrations() {
        assertThat(countAdmins("public")).isZero();
    }

    @Test
    void v6_removesAdminWithOriginalPassword_andCascadesItsData() {
        Flyway flyway = flywayFor("v6_unchanged");
        flyway.migrate();
        Long adminId = jdbc.queryForObject(
                "SELECT id FROM v6_unchanged.users WHERE email = ?", Long.class, ADMIN_EMAIL);
        jdbc.update("INSERT INTO v6_unchanged.expenses (user_id, amount, description, category, date) "
                + "VALUES (?, 10.00, 'x', 'Food', CURRENT_DATE)", adminId);

        flywayFor("v6_unchanged", null).migrate();

        assertThat(countAdmins("v6_unchanged")).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM v6_unchanged.expenses", Integer.class)).isZero();
    }

    @Test
    void v6_keepsAdminWhosePasswordWasChanged() {
        flywayFor("v6_changed").migrate();
        jdbc.update("UPDATE v6_changed.users SET password = '$2a$10$someOtherHash' WHERE email = ?", ADMIN_EMAIL);

        flywayFor("v6_changed", null).migrate();

        assertThat(countAdmins("v6_changed")).isEqualTo(1);
    }

    /** Flyway on an isolated schema, stopping just before V6. */
    private Flyway flywayFor(String schema) {
        return flywayFor(schema, "5");
    }

    private Flyway flywayFor(String schema, String target) {
        var config = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas(schema)
                .locations("classpath:db/migration");
        if (target != null) config.target(target);
        return config.load();
    }

    private int countAdmins(String schema) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + schema + ".users WHERE email = ?", Integer.class, ADMIN_EMAIL);
    }
}
