package org.commands;

import org.dataclasses.enums.MovieGenre;

/**
 * Команда для вывода элементов с заданным жанром.
 */

public class FilterByGenreCommand extends SharedCommand {

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
    public String exec(SharedCommandContext context) {
        return MovieOutputFormatter.format(
            context.visibleCollection()
                .values()
                .stream()
                .filter(movie -> movie.getGenre() != null)
                .filter(movie -> movie.getGenre().equals(genre))
        );
    }
}
