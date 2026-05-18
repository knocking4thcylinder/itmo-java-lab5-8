package org.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/**
 * Database access for users.
 */
public class UserRepository {

    private final DatabaseConnector connector;

    public UserRepository(DatabaseConnector connector) {
        this.connector = Objects.requireNonNull(
            connector,
            "Database connector cannot be null"
        );
    }

    public boolean create(String login, String passwordHash)
        throws SQLException {
        try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO users (login, password_hash)
                VALUES (?, ?)
                ON CONFLICT (login) DO NOTHING
                """)
        ) {
            statement.setString(1, login);
            statement.setString(2, passwordHash);
            return statement.executeUpdate() == 1;
        }
    }

    public Optional<String> findPasswordHash(String login)
        throws SQLException {
        try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement("""
                SELECT password_hash
                FROM users
                WHERE login = ?
                """)
        ) {
            statement.setString(1, login);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(resultSet.getString("password_hash"));
            }
        }
    }
}
