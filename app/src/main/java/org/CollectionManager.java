package org;

import java.time.LocalDateTime;
import java.util.TreeMap;
import org.dataclasses.Movie;

public class CollectionManager {

    private static CollectionManager instance;
    private TreeMap<String, Movie> collection;
    private LocalDateTime initializationDate;

    private CollectionManager() {
        this.collection = new TreeMap<>();
        this.initializationDate = LocalDateTime.now();
    }

    public static CollectionManager getInstance() {
        if (instance == null) {
            instance = new CollectionManager();
        }
        return instance;
    }

    public static void setInstance(CollectionManager manager) {
        instance = manager;
    }

    public TreeMap<String, Movie> getCollection() {
        return collection;
    }

    public void setCollection(TreeMap<String, Movie> collection) {
        this.collection = collection;
    }

    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    public int size() {
        return collection.size();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
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

    public void clear() {
        collection.clear();
    }
}
