package org.server.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;
import org.protocol.CollectionEntry;
import org.protocol.CommandRequest;
import org.protocol.CommandResponse;
import org.protocol.CommandType;
import org.protocol.MovieData;
import org.server.collection.CollectionManager;

public final class CommandProcessor {

    private static final String HELP_TEXT = """
        help : вывести справку по серверным командам
        info : вывести информацию о коллекции
        show : вывести все элементы коллекции
        insert key {element} : добавить новый элемент с заданным ключом
        update id {element} : обновить элемент по id
        clear : очистить коллекцию
        remove_key key : удалить элемент по ключу
        replace_if_lower key {element} : заменить значение по ключу, если новое значение меньше старого
        remove_greater_key key : удалить элементы с ключом больше заданного
        remove_lower_key key : удалить элементы с ключом меньше заданного
        filter_by_genre genre : вывести элементы с заданным жанром
        filter_contains_name name : вывести элементы, имя которых содержит подстроку
        filter_less_than_mpaa_rating mpaaRating : вывести элементы с рейтингом меньше заданного
        """;

    private final CollectionManager collectionManager;

    public CommandProcessor(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public CommandResponse process(CommandRequest request) {
        return switch (request.getCommandType()) {
            case HELP -> CommandResponse.success(HELP_TEXT);
            case INFO -> info();
            case SHOW -> show();
            case INSERT -> insert(request);
            case UPDATE -> update(request);
            case CLEAR -> clear();
            case REMOVE_KEY -> removeKey(request);
            case REPLACE_IF_LOWER -> replaceIfLower(request);
            case REMOVE_LOWER_KEY -> removeLowerKey(request);
            case REMOVE_GREATER_KEY -> removeGreaterKey(request);
            case FILTER_BY_GENRE -> filterByGenre(request);
            case FILTER_CONTAINS_NAME -> filterContainsName(request);
            case FILTER_LESS_THAN_MPAA_RATING -> filterLessThanMpaaRating(
                request
            );
        };
    }

    private CommandResponse info() {
        StringBuilder builder = new StringBuilder();
        builder.append("Collection type: TreeMap\n");
        builder
            .append("Initialization date: ")
            .append(collectionManager.getInitializationDate())
            .append('\n');
        builder
            .append("Number of elements: ")
            .append(collectionManager.size());

        List<LocalDateTime> creationDates = collectionManager.entryStream()
            .map(entry -> entry.getValue().getCreationDate())
            .sorted()
            .toList();
        if (!creationDates.isEmpty()) {
            builder
                .append("\nOldest movie creation date: ")
                .append(creationDates.getFirst())
                .append("\nNewest movie creation date: ")
                .append(creationDates.getLast());
        }
        return CommandResponse.success(builder.toString());
    }

    private CommandResponse show() {
        List<CollectionEntry> entries = collectionManager.toSortedEntries();
        return CommandResponse.success(
            "Returned " + entries.size() + " collection element(s)",
            entries
        );
    }

    private CommandResponse insert(CommandRequest request) {
        String key = requireStringArgument(request, "insert");
        MovieData movieData = requireMovieData(request, "insert");
        collectionManager.put(key, collectionManager.createMovie(movieData));
        return CommandResponse.success(
            "Element " + key + " successfully inserted"
        );
    }

    private CommandResponse update(CommandRequest request) {
        Integer id = requireIntegerArgument(request, "update");
        MovieData movieData = requireMovieData(request, "update");
        return collectionManager.findEntryById(id)
            .map(entry -> {
                Movie updatedMovie = collectionManager.createMovie(
                    movieData,
                    entry.getValue()
                );
                collectionManager.put(entry.getKey(), updatedMovie);
                return CommandResponse.success(
                    "Element " + entry.getKey() + " successfully updated"
                );
            })
            .orElseGet(() ->
                CommandResponse.failure("No element with that id exists")
            );
    }

    private CommandResponse clear() {
        int removedCount = collectionManager.size();
        collectionManager.clear();
        return CommandResponse.success(
            "Collection cleared, removed " + removedCount + " element(s)"
        );
    }

    private CommandResponse removeKey(CommandRequest request) {
        String key = requireStringArgument(request, "remove_key");
        if (!collectionManager.containsKey(key)) {
            return CommandResponse.failure(
                "No element with key " + key + " exists in the collection"
            );
        }
        collectionManager.remove(key);
        return CommandResponse.success("Removed element with key " + key);
    }

    private CommandResponse replaceIfLower(CommandRequest request) {
        String key = requireStringArgument(request, "replace_if_lower");
        MovieData movieData = requireMovieData(request, "replace_if_lower");
        if (!collectionManager.containsKey(key)) {
            return CommandResponse.failure(
                "No element with key " + key + " exists in the collection"
            );
        }

        Movie existingMovie = collectionManager.get(key);
        Movie newMovie = collectionManager.createMovie(movieData);
        if (newMovie.compareTo(existingMovie) < 0) {
            collectionManager.put(key, newMovie);
            return CommandResponse.success(
                "Element " + key + " successfully updated"
            );
        }
        return CommandResponse.success(
            "Element " + key + " was not replaced (new value is not lower)"
        );
    }

    private CommandResponse removeLowerKey(CommandRequest request) {
        String key = requireStringArgument(request, "remove_lower_key");
        List<String> keysToRemove = collectionManager.entryStream()
            .map(Map.Entry::getKey)
            .filter(existingKey -> existingKey.compareTo(key) < 0)
            .toList();
        keysToRemove.forEach(collectionManager::remove);
        return CommandResponse.success(
            "Removed " + keysToRemove.size() + " element(s) with keys less than " + key
        );
    }

    private CommandResponse removeGreaterKey(CommandRequest request) {
        String key = requireStringArgument(request, "remove_greater_key");
        List<String> keysToRemove = collectionManager.entryStream()
            .map(Map.Entry::getKey)
            .filter(existingKey -> existingKey.compareTo(key) > 0)
            .toList();
        keysToRemove.forEach(collectionManager::remove);
        return CommandResponse.success(
            "Removed " +
            keysToRemove.size() +
            " element(s) with keys greater than " +
            key
        );
    }

    private CommandResponse filterByGenre(CommandRequest request) {
        MovieGenre genre = MovieGenre.valueOf(
            requireStringArgument(request, "filter_by_genre")
        );
        List<CollectionEntry> entries = collectionManager.toSortedEntries(
            entry -> genre.equals(entry.getValue().getGenre())
        );
        return CommandResponse.success(
            "Matched " + entries.size() + " element(s)",
            entries
        );
    }

    private CommandResponse filterContainsName(CommandRequest request) {
        String substring = requireStringArgument(
            request,
            "filter_contains_name"
        );
        List<CollectionEntry> entries = collectionManager.toSortedEntries(
            entry -> entry.getValue().getName().contains(substring)
        );
        return CommandResponse.success(
            "Matched " + entries.size() + " element(s)",
            entries
        );
    }

    private CommandResponse filterLessThanMpaaRating(CommandRequest request) {
        MpaaRating rating = MpaaRating.valueOf(
            requireStringArgument(request, "filter_less_than_mpaa_rating")
        );
        List<CollectionEntry> entries = collectionManager.toSortedEntries(
            entry -> entry.getValue().getMpaaRating().compareTo(rating) < 0
        );
        return CommandResponse.success(
            "Matched " + entries.size() + " element(s)",
            entries
        );
    }

    private String requireStringArgument(
        CommandRequest request,
        String commandName
    ) {
        return Objects.requireNonNull(
            request.getStringArgument(),
            "Command \"" + commandName + "\" requires a string argument"
        );
    }

    private Integer requireIntegerArgument(
        CommandRequest request,
        String commandName
    ) {
        return Objects.requireNonNull(
            request.getIntegerArgument(),
            "Command \"" + commandName + "\" requires an integer argument"
        );
    }

    private MovieData requireMovieData(
        CommandRequest request,
        String commandName
    ) {
        return Objects.requireNonNull(
            request.getMovieData(),
            "Command \"" + commandName + "\" requires a movie payload"
        );
    }
}
