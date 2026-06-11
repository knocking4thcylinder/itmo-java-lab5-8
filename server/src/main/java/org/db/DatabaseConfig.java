package org.db;

import java.util.Objects;

/**
 * PostgreSQL connection settings read from process environment variables.
 */
public final class DatabaseConfig {

    public static final String HOST = valueOrDefault("DB_HOST", "pg");
    public static final int PORT = Integer.parseInt(
        valueOrDefault("DB_PORT", "5432")
    );
    public static final String DATABASE = valueOrDefault("DB_NAME", "studs");
    public static final String USER = Objects.requireNonNull(
        System.getenv("DB_USER")
    );
    public static final String PASSWORD = Objects.requireNonNull(
        System.getenv("DB_PASSWORD")
    );

    private DatabaseConfig() {}

    /**
     * Builds the PostgreSQL JDBC URL.
     *
     * @return JDBC URL
     */
    public static String jdbcUrl() {
        return "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;
    }

    private static String valueOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
