package org.commands;

import org.CollectionManager;

import java.util.Iterator;

/**
 * Команда для удаления элементов с ключом больше заданного.
 */

public class RemoveGreaterKeyCommand extends ServerCommand {

    private final String key;

    /**
     * Создает команду удаления элементов с ключом больше заданного.
     *
     * @param key пороговый ключ
     */
    public RemoveGreaterKeyCommand(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        this.key = key;
    }

    /**
     * Удаляет фильмы с ключом больше заданного.
     * @return результат выполнения
     */
    @Override
    public String exec() {
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
