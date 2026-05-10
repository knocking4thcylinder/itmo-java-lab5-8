package org.commands;

import java.util.Objects;
import org.dataclasses.Movie;

/**
 * Команда для обновления элемента коллекции по id.
 */

public class UpdateCommand extends SharedCommand {

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
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(SharedCommandContext context) throws Exception {
        var entryToUpdate = context.collectionManager()
            .getCollection()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().getId() == id)
            .findFirst()
            .orElse(null);
        if (entryToUpdate == null) {
            return "No element with that id exists";
        }

        Movie existingMovie = entryToUpdate.getValue();
        existingMovie.setName(movie.getName());
        existingMovie.setCoordinates(movie.getCoordinates());
        existingMovie.setOscarsCount(movie.getOscarsCount());
        existingMovie.setGenre(movie.getGenre());
        existingMovie.setMpaaRating(movie.getMpaaRating());
        existingMovie.setOperator(movie.getOperator());
        context.persistUpdatedMovie(entryToUpdate.getKey(), existingMovie);
        return "element " + entryToUpdate.getKey() + " successfully updated";
    }
}
