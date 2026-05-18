package org;

import org.dataclasses.Movie;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Менеджер коллекции фильмов.
 * Реализует паттерн Singleton для управления потокобезопасной коллекцией.
 */
public class CollectionManager {

    private static CollectionManager instance;
    private ConcurrentSkipListMap<String, Movie> collection;
    private LocalDateTime initializationDate;

    /**
     * Приватный конструктор для реализации Singleton.
     */
    private CollectionManager() {
        this.collection = new ConcurrentSkipListMap<>();
        this.initializationDate = LocalDateTime.now();
    }

    /**
     * Возвращает единственный экземпляр CollectionManager.
     * @return экземпляр менеджера коллекции
     */
    public static CollectionManager getInstance() {
        if (instance == null) {
            instance = new CollectionManager();
        }
        return instance;
    }

    /**
     * Устанавливает экземпляр менеджера (для тестирования).
     * @param manager экземпляр CollectionManager
     */
    public static void setInstance(CollectionManager manager) {
        instance = manager;
    }

    /**
     * Возвращает коллекцию фильмов.
     * @return потокобезопасная коллекция с фильмами
     */
    public ConcurrentSkipListMap<String, Movie> getCollection() {
        return collection;
    }

    /**
     * Устанавливает коллекцию фильмов.
     * @param collection коллекция для установки
     */
    public void setCollection(Map<String, Movie> collection) {
        this.collection = new ConcurrentSkipListMap<>(collection);
    }

    /**
     * Возвращает дату инициализации коллекции.
     * @return LocalDateTime инициализации
     */
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     * Возвращает количество элементов в коллекции.
     * @return размер коллекции
     */
    public int size() {
        return collection.size();
    }

    /**
     * Проверяет, пуста ли коллекция.
     * @return true, если коллекция пуста
     */
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    /**
     * Проверяет, содержится ли ключ в коллекции.
     * @param key ключ для проверки
     * @return true, если ключ содержится
     */
    public boolean containsKey(String key) {
        return collection.containsKey(key);
    }

    /**
     * Возвращает фильм по ключу.
     * @param key ключ фильма
     * @return фильм или null, если не найден
     */
    public Movie get(String key) {
        return collection.get(key);
    }

    /**
     * Добавляет фильм в коллекцию.
     * @param key ключ фильма
     * @param movie фильм для добавления
     * @return предыдущее значение, связанное с ключом
     */
    public Movie put(String key, Movie movie) {
        return collection.put(key, movie);
    }

    /**
     * Удаляет фильм из коллекции по ключу.
     * @param key ключ фильма
     * @return удаленный фильм или null
     */
    public Movie remove(String key) {
        return collection.remove(key);
    }

    /**
     * Очищает коллекцию.
     */
    public void clear() {
        collection.clear();
    }
}
