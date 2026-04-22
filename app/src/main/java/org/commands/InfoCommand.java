package org.commands;

import java.time.LocalDateTime;
import java.util.Comparator;
import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда вывода информации о коллекции.
 */
public class InfoCommand extends ServerCommand {

    /**
     * Выводит информацию о коллекции.
     * @param context серверный контекст
     * @return информация о коллекции
     */
    @Override
    public String exec(ServerContext context) {
        CollectionManager cm = context.collectionManager();
        StringBuilder sb = new StringBuilder();
        sb
            .append("Collection type: ")
            .append(cm.getCollection().getClass().getSimpleName())
            .append("\n");
        sb
            .append("Initialization date: ")
            .append(cm.getInitializationDate())
            .append("\n");
        sb.append("Number of elements: ").append(cm.size());
        if (!cm.isEmpty()) {
            LocalDateTime oldestCreationDate = cm
                .getCollection()
                .values()
                .stream()
                .map(Movie::getCreationDate)
                .min(Comparator.naturalOrder())
                .orElseThrow();
            LocalDateTime newestCreationDate = cm
                .getCollection()
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
