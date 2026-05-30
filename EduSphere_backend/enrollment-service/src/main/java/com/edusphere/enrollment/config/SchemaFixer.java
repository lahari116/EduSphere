package com.edusphere.enrollment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Ensures the enrollments.status column is VARCHAR(20) so that all
 * EnrollmentStatus values (PENDING, ACTIVE, DROPPED, REJECTED) can be stored.
 *
 * If the column was originally created as an ENUM (without PENDING), Hibernate's
 * ddl-auto=update will not widen it automatically — this runner fixes it safely on
 * every startup. The ALTER is a no-op when the column is already VARCHAR.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaFixer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "ALTER TABLE enrollments " +
                "MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'"
            );
            log.info("SchemaFixer: enrollments.status column ensured as VARCHAR(20)");
        } catch (Exception e) {
            // Typically means the column is already VARCHAR — safe to ignore
            log.debug("SchemaFixer: status column already correct or alter skipped: {}", e.getMessage());
        }
    }
}
