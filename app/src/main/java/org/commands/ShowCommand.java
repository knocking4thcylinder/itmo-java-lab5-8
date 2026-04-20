package org.commands;

import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда вывода всех элементов коллекции.
 */
public class ShowCommand extends ServerCommand {

    /**
     * Выводит все фильмы коллекции.
     * @return строковое представление всех фильмов
     */
    @Override
    public String exec() {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            sb.append(movie).append("\n");
        }
        return sb.toString();
    }
}
