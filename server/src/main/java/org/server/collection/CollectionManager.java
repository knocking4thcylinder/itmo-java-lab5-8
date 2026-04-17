package org.server.collection;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.dataclasses.Movie;
import org.protocol.CollectionEntry;
import org.protocol.MovieData;
import org.protocol.SerializationUtils;

public final class CollectionManager {

    private final TreeMap<String, Movie> collection = new TreeMap<>();
    private final LocalDateTime initializationDate = LocalDateTime.now();

    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    public TreeMap<String, Movie> snapshot() {
        return new TreeMap<>(collection);
    }

    public void replaceAll(TreeMap<String, Movie> loadedCollection) {
        collection.clear();
        collection.putAll(loadedCollection);
    }

    public int size() {
        return collection.size();
    }

    public void clear() {
        collection.clear();
    }

    public boolean containsKey(String key) {
        return collection.containsKey(key);
    }

    public Movie get(String key) {
        return collection.get(key);
    }

    public Movie put(String key, Movie movie) {
        return collection.put(key, movie);
    }

    public Movie remove(String key) {
        return collection.remove(key);
    }

    public Movie createMovie(MovieData movieData) {
        return Movie.fromData(movieData);
    }

    public Movie createMovie(MovieData movieData, Movie existingMovie) {
        return Movie.fromData(
            movieData,
            existingMovie.getId(),
            existingMovie.getCreationDate()
        );
    }

    public Optional<Map.Entry<String, Movie>> findEntryById(int id) {
        return entryStream()
            .filter(entry -> entry.getValue().getId() == id)
            .findFirst();
    }

    public List<CollectionEntry> toSortedEntries() {
        return toSortedEntries(entryStream());
    }

    public List<CollectionEntry> toSortedEntries(
        Predicate<Map.Entry<String, Movie>> predicate
    ) {
        return toSortedEntries(entryStream().filter(predicate));
    }

    public Stream<Map.Entry<String, Movie>> entryStream() {
        return collection.entrySet().stream();
    }

    private List<CollectionEntry> toSortedEntries(
        Stream<Map.Entry<String, Movie>> entryStream
    ) {
        return entryStream
            .map(entry -> new CollectionEntry(entry.getKey(), entry.getValue()))
            .sorted(
                Comparator.comparingInt(
                    (CollectionEntry entry) ->
                        SerializationUtils.serializedSize(entry)
                )
                    .thenComparing(CollectionEntry::key)
            )
            .toList();
    }
}
