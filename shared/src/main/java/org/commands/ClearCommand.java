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
        int collectionLength = context.collectionManager().size();
        context.persistClearedCollection();
        context.collectionManager().clear();
        return "collection cleared, removed " + collectionLength + " elements";
    }
}
