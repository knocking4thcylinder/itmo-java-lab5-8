package org.commands;

import org.CollectionManager;

/**
 * Команда для очистки коллекции.
 */

public class ClearCommand extends ServerCommand {

    /**
     * Очищает коллекцию.
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(ServerContext context) {
        int collectionLength = context.collectionManager().size();
        context.collectionManager().clear();
        return "collection cleared, removed " + collectionLength + " elements";
    }
}
