package org.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates PostgreSQL JDBC connections.
 */
public class DatabaseConnector {

    /**
     * Opens a new database connection.
     *
     * @return JDBC connection
     * @throws SQLException if the connection cannot be opened
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            DatabaseConfig.jdbcUrl(),
            DatabaseConfig.USER,
            DatabaseConfig.PASSWORD
        );
    }
}
