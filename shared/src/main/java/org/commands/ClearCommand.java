package org.commands;

/**
 * Команда для очистки коллекции.
 */

public class ClearCommand extends SharedCommand {

    /**
     * Очищает коллекцию.
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(SharedCommandContext context) throws Exception {
        var removedKeys = context.persistClearedCollection();
        removedKeys.forEach(context.collectionManager()::remove);
        return "collection cleared, removed " + removedKeys.size() + " elements";
    }
}
