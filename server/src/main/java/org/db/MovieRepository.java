package org.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.dataclasses.Coordinates;
import org.dataclasses.Location;
import org.dataclasses.Movie;
import org.dataclasses.Person;
import org.dataclasses.enums.Country;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;

/**
 * Database access for movies.
 */
public class MovieRepository {

    private final DatabaseConnector connector;

    public MovieRepository(DatabaseConnector connector) {
        this.connector = Objects.requireNonNull(
            connector,
            "Database connector cannot be null"
        );
    }

    public TreeMap<String, Movie> loadAll() throws SQLException {
        TreeMap<String, Movie> movies = new TreeMap<>();
        try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement("""
                SELECT key, id, name, coordinates_x, coordinates_y,
                       creation_date, oscars_count, genre, mpaa_rating,
                       operator_name, operator_weight, operator_passport_id,
                       operator_nationality, operator_location_x,
                       operator_location_y, operator_location_name
                FROM movies
                ORDER BY key
                """);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Map.Entry<String, Movie> entry = mapMovie(resultSet);
                movies.put(entry.getKey(), entry.getValue());
            }
        }
        return movies;
    }

    public int insert(String key, String ownerLogin, Movie movie)
        throws SQLException {
        LocalDateTime creationDate = movie.getCreationDate() == null
            ? LocalDateTime.now()
            : movie.getCreationDate();
        try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO movies (
                    key, owner_login, name, coordinates_x, coordinates_y,
                    creation_date, oscars_count, genre, mpaa_rating,
                    operator_name, operator_weight, operator_passport_id,
                    operator_nationality, operator_location_x,
                    operator_location_y, operator_location_name
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """)
        ) {
            fillMovieStatement(statement, key, ownerLogin, movie, creationDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                int generatedId = resultSet.getInt("id");
                movie.restoreGeneratedFields(generatedId, creationDate);
                return generatedId;
            }
        }
    }

    public boolean updateByKey(String key, Movie movie) throws SQLException {
        try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement("""
                UPDATE movies
                SET name = ?,
                    coordinates_x = ?,
                    coordinates_y = ?,
                    oscars_count = ?,
                    genre = ?,
                    mpaa_rating = ?,
                    operator_name = ?,
                    operator_weight = ?,
                    operator_passport_id = ?,
                    operator_nationality = ?,
                    operator_location_x = ?,
                    operator_location_y = ?,
                    operator_location_name = ?
                WHERE key = ?
                """)
        ) {
            statement.setString(1, movie.getName());
            statement.setInt(2, movie.getCoordinates().getX());
            statement.setInt(3, movie.getCoordinates().getY());
            fillMutableMovieStatement(statement, movie, 4);
            statement.setString(14, key);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean removeByKey(String key) throws SQLException {
        try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM movies
                WHERE key = ?
                """)
        ) {
            statement.setString(1, key);
            return statement.executeUpdate() == 1;
        }
    }

    public int removeByKeys(Collection<String> keys) throws SQLException {
        if (keys.isEmpty()) {
            return 0;
        }

        try (Connection connection = connector.getConnection()) {
            int removedCount = 0;
            try (
                PreparedStatement statement = connection.prepareStatement("""
                    DELETE FROM movies
                    WHERE key = ?
                    """)
            ) {
                for (String key : keys) {
                    statement.setString(1, key);
                    statement.addBatch();
                }
                int[] results = statement.executeBatch();
                for (int result : results) {
                    if (result > 0) {
                        removedCount += result;
                    }
                }
            }
            return removedCount;
        }
    }

    public int clear() throws SQLException {
        try (
            Connection connection = connector.getConnection();
            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM movies
                """)
        ) {
            return statement.executeUpdate();
        }
    }

    private Map.Entry<String, Movie> mapMovie(ResultSet resultSet)
        throws SQLException {
        Movie movie = new Movie(
            resultSet.getString("name"),
            new Coordinates(
                resultSet.getInt("coordinates_x"),
                resultSet.getInt("coordinates_y")
            ),
            resultSet.getInt("oscars_count"),
            MovieGenre.valueOf(resultSet.getString("genre")),
            MpaaRating.valueOf(resultSet.getString("mpaa_rating")),
            mapOperator(resultSet)
        );
        movie.restoreGeneratedFields(
            resultSet.getInt("id"),
            resultSet.getTimestamp("creation_date").toLocalDateTime()
        );
        return Map.entry(resultSet.getString("key"), movie);
    }

    private Person mapOperator(ResultSet resultSet) throws SQLException {
        String operatorName = resultSet.getString("operator_name");
        if (operatorName == null) {
            return null;
        }

        Location location = null;
        Long locationX = nullableLong(resultSet, "operator_location_x");
        Double locationY = nullableDouble(resultSet, "operator_location_y");
        if (locationX != null && locationY != null) {
            location = new Location(
                locationX,
                locationY,
                resultSet.getString("operator_location_name")
            );
        }

        return new Person(
            operatorName,
            resultSet.getDouble("operator_weight"),
            resultSet.getString("operator_passport_id"),
            Country.valueOf(resultSet.getString("operator_nationality")),
            location
        );
    }

    private void fillMovieStatement(
        PreparedStatement statement,
        String key,
        String ownerLogin,
        Movie movie,
        LocalDateTime creationDate
    ) throws SQLException {
        statement.setString(1, key);
        statement.setString(2, ownerLogin);
        statement.setString(3, movie.getName());
        statement.setInt(4, movie.getCoordinates().getX());
        statement.setInt(5, movie.getCoordinates().getY());
        statement.setTimestamp(6, Timestamp.valueOf(creationDate));
        fillMutableMovieStatement(statement, movie, 7);
    }

    private void fillMutableMovieStatement(
        PreparedStatement statement,
        Movie movie,
        int startIndex
    ) throws SQLException {
        statement.setInt(startIndex, movie.getOscarsCount());
        statement.setString(startIndex + 1, movie.getGenre().name());
        statement.setString(startIndex + 2, movie.getMpaaRating().name());

        Person operator = movie.getOperator();
        if (operator == null) {
            for (int i = startIndex + 3; i <= startIndex + 9; i++) {
                statement.setObject(i, null);
            }
            return;
        }

        statement.setString(startIndex + 3, operator.getName());
        statement.setDouble(startIndex + 4, operator.getWeight());
        statement.setString(startIndex + 5, operator.getPassportID());
        statement.setString(startIndex + 6, operator.getNationality().name());

        Location location = operator.getLocation();
        if (location == null) {
            statement.setObject(startIndex + 7, null);
            statement.setObject(startIndex + 8, null);
            statement.setString(startIndex + 9, null);
        } else {
            statement.setLong(startIndex + 7, location.getX());
            statement.setDouble(startIndex + 8, location.getY());
            statement.setString(startIndex + 9, location.getName());
        }
    }

    private Long nullableLong(ResultSet resultSet, String column)
        throws SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private Double nullableDouble(ResultSet resultSet, String column)
        throws SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }
}
