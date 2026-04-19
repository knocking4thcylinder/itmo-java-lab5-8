package org.commands;

import java.io.Serializable;
import org.CollectionManager;

/**
 * Команда для удаления элемента по ключу.
 */

public class RemoveKeyCommand implements Executable, Serializable {

    private static final long serialVersionUID = 1L;

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
     * @return результат выполнения
     */
    @Override
    public String exec() {
        if (!CollectionManager.getInstance().containsKey(key)) {
            return "no element with key " + key + " exists in the collection";
        }
        CollectionManager.getInstance().remove(key);
        return "removed element with key " + key;
    }
}
