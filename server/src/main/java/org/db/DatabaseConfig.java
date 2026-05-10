package org.db;

import java.util.Objects;

/**
 * PostgreSQL connection settings read from process environment variables.
 */
public final class DatabaseConfig {

    public static final String HOST = "pg";
    public static final int PORT = 5432;
    public static final String DATABASE = "studs";
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
}
