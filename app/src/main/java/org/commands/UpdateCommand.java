package org.commands;

import java.util.Objects;
import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда для обновления элемента коллекции по id.
 */

public class UpdateCommand extends ServerCommand {

    private final int id;
    private final Movie movie;

    /**
     * Создает команду обновления элемента по id.
     *
     * @param id идентификатор фильма
     * @param movie новые данные фильма
     */
    public UpdateCommand(int id, Movie movie) {
        if (id <= 0) {
            throw new IllegalArgumentException("Id must be greater than zero");
        }
        this.id = id;
        this.movie = Objects.requireNonNull(movie, "Movie cannot be null");
    }

    /**
     * Обновляет фильм по id.
     * @return результат выполнения
     */
    @Override
    public String exec() {
        var collection = CollectionManager.getInstance().getCollection();
        for (var entry : collection.entrySet()) {
            if (entry.getValue().getId() == id) {
                Movie existingMovie = entry.getValue();
                existingMovie.setName(movie.getName());
                existingMovie.setCoordinates(movie.getCoordinates());
                existingMovie.setOscarsCount(movie.getOscarsCount());
                existingMovie.setGenre(movie.getGenre());
                existingMovie.setMpaaRating(movie.getMpaaRating());
                existingMovie.setOperator(movie.getOperator());
                return "element " + entry.getKey() + " successfully updated";
            }
        }
        return "No element with that id exists";
    }
}
