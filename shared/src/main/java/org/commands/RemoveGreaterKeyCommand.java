package org.commands;

import java.util.List;

/**
 * Команда для удаления элементов с ключом больше заданного.
 */

public class RemoveGreaterKeyCommand extends SharedCommand {

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
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(SharedCommandContext context) throws Exception {
        var collection = context.collectionManager().getCollection();
        List<String> keysToRemove = context.visibleCollection()
            .keySet()
            .stream()
            .filter(k -> k.compareTo(key) > 0)
            .toList();
        var removedKeys = context.persistRemovedMovies(keysToRemove);
        removedKeys.forEach(collection::remove);
        return (
            "removed " +
            removedKeys.size() +
            " elements with keys greater than " +
            key
        );
    }
}
