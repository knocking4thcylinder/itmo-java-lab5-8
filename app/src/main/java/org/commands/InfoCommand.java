package org.commands;

import java.time.LocalDateTime;
import org.App;
import org.dataclasses.Movie;

public class InfoCommand implements Executable {

    private static LocalDateTime initializationDate = LocalDateTime.now();

    @Override
    public String exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"info\" does not accept any arguments"
            );
        }
        StringBuilder sb = new StringBuilder();
        sb
            .append("Collection type: ")
            .append(App.getCollection().getClass().getSimpleName())
            .append("\n");
        sb
            .append("Initialization date: ")
            .append(initializationDate)
            .append("\n");
        sb.append("Number of elements: ").append(App.getCollection().size());
        if (!App.getCollection().isEmpty()) {
            LocalDateTime oldestCreationDate = null;
            LocalDateTime newestCreationDate = null;
            for (Movie movie : App.getCollection().values()) {
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
