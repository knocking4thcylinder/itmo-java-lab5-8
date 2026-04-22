package org.commands;

/**
 * Команда для удаления элементов с ключом меньше заданного.
 */

public class RemoveLowerKeyCommand extends ServerCommand {

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
    public String exec(ServerContext context) {
        var collection = context.collectionManager().getCollection();
        long removedCount = collection
            .keySet()
            .stream()
            .filter(k -> k.compareTo(key) < 0)
            .count();
        collection.entrySet().removeIf(entry -> entry.getKey().compareTo(key) < 0);
        return (
            "removed " + removedCount + " elements with keys less than " + key
        );
    }
}
