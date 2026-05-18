package org.commands;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dataclasses.Movie;

/**
 * Утилиты форматирования вывода коллекции фильмов.
 */
final class MovieOutputFormatter {

    private static final Comparator<Movie> BY_RENDERED_SIZE = Comparator
        .comparingInt(MovieOutputFormatter::renderedSize)
        .thenComparing(Movie::compareTo);

    private MovieOutputFormatter() {}

    /**
     * Сортирует фильмы по размеру строкового представления и объединяет их в ответ.
     *
     * @param movies поток фильмов
     * @return строка результата
     */
    static String format(Stream<Movie> movies) {
        return movies
            .sorted(BY_RENDERED_SIZE)
            .map(Movie::toString)
            .collect(Collectors.joining("\n"));
    }

    private static int renderedSize(Movie movie) {
        return movie.toString().length();
    }
}
