package org.commands;

import org.CollectionManager;
import org.dataclasses.Movie;

import java.time.LocalDateTime;

/**
 * Команда вывода информации о коллекции.
 */
public class InfoCommand extends ServerCommand {

    /**
     * Выводит информацию о коллекции.
     * @return информация о коллекции
     */
    @Override
    public String exec() {
        CollectionManager cm = CollectionManager.getInstance();
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
            LocalDateTime oldestCreationDate = null;
            LocalDateTime newestCreationDate = null;
            for (Movie movie : cm.getCollection().values()) {
                if (
                    oldestCreationDate == null ||
                    movie.getCreationDate().isBefore(oldestCreationDate)
                ) {
                    oldestCreationDate = movie.getCreationDate();
                }
                if (
                    newestCreationDate == null ||
                    movie.getCreationDate().isAfter(newestCreationDate)
                ) {
                    newestCreationDate = movie.getCreationDate();
                }
            }
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
