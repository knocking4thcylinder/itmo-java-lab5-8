package org.commands;

import java.io.Serializable;
import org.CollectionManager;

/**
 * Команда для очистки коллекции.
 */

public class ClearCommand implements Executable, Serializable {

    private static final long serialVersionUID = 1L;

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
