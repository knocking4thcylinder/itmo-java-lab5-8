package org.commands;

import java.util.Objects;
import org.dataclasses.Movie;

/**
 * Команда для добавления нового элемента с заданным ключом.
 */

public class InsertCommand extends ServerCommand {

    private final String key;
    private final Movie movie;

    /**
     * Создает команду добавления нового элемента.
     *
     * @param key ключ элемента
     * @param movie фильм для вставки
     */
    public InsertCommand(String key, Movie movie) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        this.key = key;
        this.movie = Objects.requireNonNull(movie, "Movie cannot be null");
    }

    /**
     * Добавляет фильм с заданным ключом.
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(ServerContext context) {
        var collection = context.collectionManager().getCollection();
        movie.assignGeneratedFields();
        collection.put(key, movie);
        return "element " + key + " successfully inserted";
    }
}
