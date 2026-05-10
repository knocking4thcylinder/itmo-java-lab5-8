package org.commands;

import java.time.LocalDateTime;
import java.util.Comparator;
import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда вывода информации о коллекции.
 */
public class InfoCommand extends SharedCommand {

    /**
     * Выводит информацию о коллекции.
     * @param context серверный контекст
     * @return информация о коллекции
     */
    @Override
    public String exec(SharedCommandContext context) {
        CollectionManager cm = context.collectionManager();
        var visibleCollection = context.visibleCollection();
        StringBuilder sb = new StringBuilder();
        sb
            .append("Collection type: ")
            .append(cm.getCollection().getClass().getSimpleName())
            .append("\n");
        sb
            .append("Initialization date: ")
            .append(cm.getInitializationDate())
            .append("\n");
        sb.append("Number of elements: ").append(visibleCollection.size());
        if (!visibleCollection.isEmpty()) {
            LocalDateTime oldestCreationDate = visibleCollection
                .values()
                .stream()
                .map(Movie::getCreationDate)
                .min(Comparator.naturalOrder())
                .orElseThrow();
            LocalDateTime newestCreationDate = visibleCollection
                .values()
                .stream()
                .map(Movie::getCreationDate)
                .max(Comparator.naturalOrder())
                .orElseThrow();
            sb
                .append("\nOldest movie creation date: ")
                .append(oldestCreationDate);
            sb
                .append("\nNewest movie creation date: ")
                .append(newestCreationDate);
        }
        return sb.toString();
    }
}
