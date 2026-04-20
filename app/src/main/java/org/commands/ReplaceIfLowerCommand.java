package org.commands;

import java.util.Objects;
import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда для замены значения по ключу, если новое меньше старого.
 */

public class ReplaceIfLowerCommand extends ServerCommand {

    private final String key;
    private final Movie movie;

    /**
     * Создает команду замены, если новое значение меньше старого.
     *
     * @param key ключ элемента
     * @param movie новый фильм
     */
    public ReplaceIfLowerCommand(String key, Movie movie) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        this.key = key;
        this.movie = Objects.requireNonNull(movie, "Movie cannot be null");
    }

    /**
     * Заменяет фильм если новое значение меньше старого.
     * @return результат выполнения
     */
    @Override
    public String exec() {
        var collection = CollectionManager.getInstance().getCollection();
        if (!collection.containsKey(key)) {
            return "no element with key " + key + " exists in the collection";
        }
        if (movie.compareTo(collection.get(key)) < 0) {
            collection.put(key, movie);
            return "element " + key + " successfully updated";
        }
        return "element " + key + " was not replaced (new value is not lower)";
    }
}
