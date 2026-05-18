package org.commands;

/**
 * Команда вывода всех элементов коллекции.
 */
public class ShowCommand extends SharedCommand {

    /**
     * Выводит все фильмы коллекции.
     * @param context серверный контекст
     * @return строковое представление всех фильмов
     */
    @Override
    public String exec(SharedCommandContext context) {
        return MovieOutputFormatter.format(
            context.visibleCollection().values().stream()
        );
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
