package org.commands;

import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;

/**
 * Команда для вывода элементов с заданным жанром.
 */

public class FilterByGenreCommand extends ServerCommand {

    private final MovieGenre genre;

    /**
     * Создает команду фильтрации по жанру.
     *
     * @param genre жанр для фильтрации
     */
    public FilterByGenreCommand(MovieGenre genre) {
        this.genre = java.util.Objects.requireNonNull(
            genre,
            "Genre cannot be null"
        );
    }

    /**
     * Фильтрует фильмы по жанру.
     * @param context серверный контекст
     * @return отфильтрованные фильмы
     */
    @Override
    public String exec(ServerContext context) {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : context.collectionManager()
            .getCollection()
            .values()) {
            if (movie.getGenre() != null && movie.getGenre().equals(genre)) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
