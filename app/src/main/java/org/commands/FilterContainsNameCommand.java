package org.commands;

import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда для вывода элементов, имя которых содержит подстроку.
 */

public class FilterContainsNameCommand extends ServerCommand {

    private final String namePart;

    /**
     * Создает команду фильтрации по подстроке имени.
     *
     * @param namePart подстрока имени
     */
    public FilterContainsNameCommand(String namePart) {
        if (namePart == null || namePart.isBlank()) {
            throw new IllegalArgumentException(
                "Name filter cannot be null or blank"
            );
        }
        this.namePart = namePart;
    }

    /**
     * Фильтрует фильмы по имени (содержит подстроку).
     * @return отфильтрованные фильмы
     */
    @Override
    public String exec() {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            if (
                movie.getName() != null &&
                movie.getName().contains(namePart)
            ) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
