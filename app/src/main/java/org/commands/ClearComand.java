package org.commands;

import org.CollectionManager;

/**
 * Команда для очистки коллекции.
 */

public class ClearComand implements Executable {

    /**
     * Очищает коллекцию.
     * @param args аргументы команды
     * @return результат выполнения
     */
    @Override
    public String exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"clear\" does not accept any arguments"
            );
        }

        int collectionLength = CollectionManager.getInstance().size();
        CollectionManager.getInstance().clear();
        return "collection cleared, removed " + collectionLength + " elements";
    }
}
