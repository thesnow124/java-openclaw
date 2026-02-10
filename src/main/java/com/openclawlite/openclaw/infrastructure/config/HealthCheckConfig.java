package com.openclawlite.openclaw.infrastructure.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Custom health indicators for OpenClaw Lite
 */
@Component
public class HealthCheckConfig {

    /**
     * Database health check
     */
    @Component
    public static class DatabaseHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                String dbPath = "data/openclaw.db";
                File dbFile = new File(dbPath);

                if (!dbFile.exists()) {
                    return Health.down()
                        .withDetail("database", "Database file not found")
                        .withDetail("path", dbPath)
                        .build();
                }

                // Check if database is readable/writable
                if (!dbFile.canRead()) {
                    return Health.down()
                        .withDetail("database", "Database not readable")
                        .withDetail("path", dbPath)
                        .build();
                }

                // Check size
                long size = dbFile.length();
                long sizeMB = size / (1024 * 1024);

                return Health.up()
                    .withDetail("database", "SQLite database OK")
                    .withDetail("path", dbPath)
                    .withDetail("sizeMB", sizeMB)
                    .build();

            } catch (Exception e) {
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }
    }

    /**
     * Plugins directory health check
     */
    @Component
    public static class PluginsHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                Path pluginsDir = Path.of("plugins");

                if (!Files.exists(pluginsDir)) {
                    return Health.up()
                        .withDetail("plugins", "Plugins directory not created yet")
                        .build();
                }

                int pluginCount = 0;
                if (Files.exists(pluginsDir)) {
                    File[] files = pluginsDir.toFile().listFiles((dir, name) -> name.endsWith(".jar"));
                    pluginCount = files.length;
                }

                return Health.up()
                    .withDetail("plugins", "Plugins directory OK")
                    .withDetail("path", pluginsDir.toAbsolutePath())
                    .withDetail("count", pluginCount)
                    .build();

            } catch (Exception e) {
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }
    }

    /**
     * Data directory health check
     */
    @Component
    public static class DataDirectoryHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                Path dataDir = Path.of("data");

                if (!Files.exists(dataDir)) {
                    return Health.down()
                        .withDetail("data", "Data directory not found")
                        .build();
                }

                // Check subdirectories
                boolean pluginsOk = Files.exists(dataDir.resolve("plugins"));
                boolean agentsOk = Files.exists(dataDir.resolve("agents"));
                boolean sessionsOk = Files.exists(dataDir.resolve("sessions"));

                return Health.up()
                    .withDetail("data", "Data directory OK")
                    .withDetail("path", dataDir.toAbsolutePath())
                    .withDetail("plugins", pluginsOk ? "OK" : "Not created")
                    .withDetail("agents", agentsOk ? "OK" : "Not created")
                    .withDetail("sessions", sessionsOk ? "OK" : "Not created")
                    .build();

            } catch (Exception e) {
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }
    }
}
