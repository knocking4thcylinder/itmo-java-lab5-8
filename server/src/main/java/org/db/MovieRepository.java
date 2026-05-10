package org.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        statement.setInt(7, movie.getOscarsCount());
        statement.setString(8, movie.getGenre().name());
        statement.setString(9, movie.getMpaaRating().name());

        Person operator = movie.getOperator();
        if (operator == null) {
            for (int i = 10; i <= 16; i++) {
                statement.setObject(i, null);
            }
            return;
        }

        statement.setString(10, operator.getName());
        statement.setDouble(11, operator.getWeight());
        statement.setString(12, operator.getPassportID());
        statement.setString(13, operator.getNationality().name());

        Location location = operator.getLocation();
        if (location == null) {
            statement.setObject(14, null);
            statement.setObject(15, null);
            statement.setString(16, null);
        } else {
            statement.setLong(14, location.getX());
            statement.setDouble(15, location.getY());
            statement.setString(16, location.getName());
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
