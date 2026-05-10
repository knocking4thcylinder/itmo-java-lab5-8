package org.commands;

import java.util.List;

/**
 * Команда для удаления элементов с ключом меньше заданного.
 */

public class RemoveLowerKeyCommand extends SharedCommand {

    private final String key;

    /**
     * Создает команду удаления элементов с ключом меньше заданного.
     *
     * @param key пороговый ключ
     */
    public RemoveLowerKeyCommand(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        this.key = key;
    }

    /**
     * Удаляет фильмы с ключом меньше заданного.
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(SharedCommandContext context) throws Exception {
        var collection = context.collectionManager().getCollection();
        List<String> keysToRemove = collection
            .keySet()
            .stream()
            .filter(k -> k.compareTo(key) < 0)
            .toList();
        context.persistRemovedMovies(keysToRemove);
        keysToRemove.forEach(collection::remove);
        return (
            "removed " + keysToRemove.size() + " elements with keys less than " + key
        );
    }
}
