package org.commands;

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
        return MovieOutputFormatter.format(
            context.collectionManager().getCollection().values().stream()
        );
    }
}
