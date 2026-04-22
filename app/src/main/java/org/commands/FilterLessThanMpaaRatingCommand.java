package org.commands;

import org.dataclasses.Movie;
import org.dataclasses.enums.MpaaRating;

/**
 * Команда для вывода элементов с рейтингом меньше заданного.
 */

public class FilterLessThanMpaaRatingCommand extends ServerCommand {

    private final MpaaRating mpaaRating;

    /**
     * Создает команду фильтрации по рейтингу MPAA.
     *
     * @param mpaaRating рейтинг для сравнения
     */
    public FilterLessThanMpaaRatingCommand(MpaaRating mpaaRating) {
        this.mpaaRating = java.util.Objects.requireNonNull(
            mpaaRating,
            "Mpaa rating cannot be null"
        );
    }

    /**
     * Фильтрует фильмы по рейтингу (меньше заданного).
     * @param context серверный контекст
     * @return отфильтрованные фильмы
     */
    @Override
    public String exec(ServerContext context) {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : context.collectionManager()
            .getCollection()
            .values()) {
            if (movie.getMpaaRating().compareTo(mpaaRating) < 0) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
