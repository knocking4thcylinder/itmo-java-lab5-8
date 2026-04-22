package org.commands;

import java.nio.file.Path;
import java.util.Objects;
import org.CollectionManager;

/**
 * Серверный контекст выполнения команд.
 */
public class ServerContext {

    private final CollectionManager collectionManager;
    private final Path storagePath;

    /**
     * Создает новый серверный контекст.
     *
     * @param collectionManager менеджер коллекции
     * @param storagePath путь к файлу хранения
     */
    public ServerContext(
        CollectionManager collectionManager,
        Path storagePath
    ) {
        this.collectionManager = Objects.requireNonNull(
            collectionManager,
            "Collection manager cannot be null"
        );
        this.storagePath = Objects.requireNonNull(
            storagePath,
            "Storage path cannot be null"
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

    /**
     * Возвращает путь к файлу хранения.
     *
     * @return путь к файлу хранения
     */
    public Path storagePath() {
        return storagePath;
    }
}
