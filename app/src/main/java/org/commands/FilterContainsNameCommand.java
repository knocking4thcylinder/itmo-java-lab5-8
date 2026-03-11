package org.commands;

import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда для вывода элементов, имя которых содержит подстроку.
 */

public class FilterContainsNameCommand implements Executable {

    /**
     * Фильтрует фильмы по имени (содержит подстроку).
     * @param args аргументы команды, где args[0] - подстрока
     * @return отфильтрованные фильмы
     */
    @Override
    public String exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"filter_contains_name\" accepts exactly one argument"
            );
        }
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            if (movie.getName() != null && movie.getName().contains(args[0])) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
