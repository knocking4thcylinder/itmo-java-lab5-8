package org.commands;

import org.CollectionManager;

import java.util.Iterator;

/**
 * Команда для удаления элементов с ключом больше заданного.
 */

public class RemoveGreaterKeyCommand implements Executable {

    /**
     * Удаляет фильмы с ключом больше заданного.
     * @param args аргументы команды, где args[0] - ключ
     * @return результат выполнения
     */
    @Override
    public String exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"remove_greater_key\" accepts exactly one argument"
            );
        }
        String key = args[0];
        var collection = CollectionManager.getInstance().getCollection();
        Iterator<String> iterator = collection.keySet().iterator();
        int removedCount = 0;
        while (iterator.hasNext()) {
            String k = iterator.next();
            if (k.compareTo(key) > 0) {
                iterator.remove();
                removedCount++;
            }
        }
        return (
            "removed " +
            removedCount +
            " elements with keys greater than " +
            key
        );
    }
}
