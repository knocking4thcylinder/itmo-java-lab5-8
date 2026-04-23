package org.commands;

/**
 * Команда для удаления элемента по ключу.
 */

public class RemoveKeyCommand extends ServerCommand {

    private final String key;

    /**
     * Создает команду удаления элемента по ключу.
     *
     * @param key ключ элемента
     */
    public RemoveKeyCommand(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        this.key = key;
    }

    /**
     * Удаляет фильм по ключу.
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(ServerContext context) {
        if (!context.collectionManager().containsKey(key)) {
            return "no element with key " + key + " exists in the collection";
        }
        context.collectionManager().remove(key);
        return "removed element with key " + key;
    }
}
