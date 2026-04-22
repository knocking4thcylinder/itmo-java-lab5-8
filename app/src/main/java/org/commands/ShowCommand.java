package org.commands;

import org.dataclasses.Movie;

/**
 * Команда вывода всех элементов коллекции.
 */
public class ShowCommand extends ServerCommand {

    /**
     * Выводит все фильмы коллекции.
     * @param context серверный контекст
     * @return строковое представление всех фильмов
     */
    @Override
    public String exec(ServerContext context) {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : context.collectionManager()
            .getCollection()
            .values()) {
            sb.append(movie).append("\n");
        }
        return sb.toString();
    }
}
