package org.commands;

import java.io.Serializable;
import org.CollectionManager;
import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;

/**
 * Команда для вывода элементов с заданным жанром.
 */

public class FilterByGenreCommand implements Executable, Serializable {

    private static final long serialVersionUID = 1L;

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
     * @return отфильтрованные фильмы
     */
    @Override
    public String exec() {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            if (movie.getGenre() != null && movie.getGenre().equals(genre)) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
