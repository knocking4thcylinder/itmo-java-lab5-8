package org.commands;

import java.util.Objects;
import org.CollectionManager;

/**
 * Серверный контекст выполнения команд.
 */
public class ServerContext implements SharedCommandContext {

    private final CollectionManager collectionManager;

    /**
     * Создает новый серверный контекст.
     *
     * @param collectionManager менеджер коллекции
     */
    public ServerContext(CollectionManager collectionManager) {
        this.collectionManager = Objects.requireNonNull(
            collectionManager,
            "Collection manager cannot be null"
        );
    }

    /**
     * Возвращает менеджер коллекции.
     *
     * @return менеджер коллекции
     */
    public CollectionManager collectionManager() {
        return collectionManager;
    }
}
