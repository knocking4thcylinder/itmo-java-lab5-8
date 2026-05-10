package org.commands;

import java.util.Collection;
import java.util.Objects;
import org.CollectionManager;
import org.dataclasses.Movie;
import org.db.MovieRepository;

/**
 * Серверный контекст выполнения команд.
 */
public class ServerContext implements SharedCommandContext {

    private final CollectionManager collectionManager;
    private final MovieRepository movieRepository;
    private final String ownerLogin;

    /**
     * Создает новый серверный контекст.
     *
     * @param collectionManager менеджер коллекции
     * @param movieRepository репозиторий фильмов
     * @param ownerLogin логин владельца для команд без авторизации
     */
    public ServerContext(
        CollectionManager collectionManager,
        MovieRepository movieRepository,
        String ownerLogin
    ) {
        this.collectionManager = Objects.requireNonNull(
            collectionManager,
            "Collection manager cannot be null"
        );
        this.movieRepository = Objects.requireNonNull(
            movieRepository,
            "Movie repository cannot be null"
        );
        this.ownerLogin = Objects.requireNonNull(
            ownerLogin,
            "Owner login cannot be null"
        );
    }

    /**
     * Возвращает менеджер коллекции.
     *
     * @return менеджер коллекции
     */
    public CollectionManager collectionManager() {
        return collectionManager;
    }

    /**
     * Возвращает логин владельца текущего запроса.
     *
     * @return логин владельца
     */
    public String ownerLogin() {
        return ownerLogin;
    }

    /**
     * Возвращает репозиторий фильмов.
     *
     * @return репозиторий фильмов
     */
    public MovieRepository movieRepository() {
        return movieRepository;
    }

    @Override
    public void persistInsertedMovie(String key, Movie movie) throws Exception {
        movieRepository.insert(key, ownerLogin, movie);
    }

    @Override
    public void persistUpdatedMovie(String key, Movie movie) throws Exception {
        movieRepository.updateByKey(key, movie);
    }

    @Override
    public void persistRemovedMovie(String key) throws Exception {
        movieRepository.removeByKey(key);
    }

    @Override
    public void persistRemovedMovies(Collection<String> keys) throws Exception {
        movieRepository.removeByKeys(keys);
    }

    @Override
    public void persistClearedCollection() throws Exception {
        movieRepository.clear();
    }
}
