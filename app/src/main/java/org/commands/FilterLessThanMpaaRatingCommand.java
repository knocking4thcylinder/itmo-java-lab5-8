package org.commands;

import java.io.Serializable;
import org.CollectionManager;
import org.dataclasses.Movie;
import org.dataclasses.enums.MpaaRating;

/**
 * Команда для вывода элементов с рейтингом меньше заданного.
 */

public class FilterLessThanMpaaRatingCommand
    implements Executable, Serializable {

    private static final long serialVersionUID = 1L;

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
     * @return отфильтрованные фильмы
     */
    @Override
    public String exec() {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            if (movie.getMpaaRating().compareTo(mpaaRating) < 0) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
