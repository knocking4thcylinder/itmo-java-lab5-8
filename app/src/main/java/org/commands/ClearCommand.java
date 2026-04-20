package org.commands;

import org.CollectionManager;

/**
 * Команда для очистки коллекции.
 */

public class ClearCommand extends ServerCommand {

    /**
     * Очищает коллекцию.
     * @return результат выполнения
     */
    @Override
    public String exec() {
        int collectionLength = CollectionManager.getInstance().size();
        CollectionManager.getInstance().clear();
        return "collection cleared, removed " + collectionLength + " elements";
    }
}
