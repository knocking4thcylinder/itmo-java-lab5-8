package org.commands;

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
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(ServerContext context) {
        var collection = context.collectionManager().getCollection();
        long removedCount = collection
            .keySet()
            .stream()
            .filter(k -> k.compareTo(key) > 0)
            .count();
        collection.entrySet().removeIf(entry -> entry.getKey().compareTo(key) > 0);
        return (
            "removed " +
            removedCount +
            " elements with keys greater than " +
            key
        );
    }
}
