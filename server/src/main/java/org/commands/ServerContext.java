package org.commands;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.CollectionManager;
import org.auth.PasswordHasher;
import org.dataclasses.Movie;
import org.db.MovieRepository;
import org.db.UserRepository;
import org.shared.AuthResult;

/**
 * Серверный контекст выполнения команд.
 */
public class ServerContext implements SharedCommandContext {

    private final CollectionManager collectionManager;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final Map<String, String> ownersByKey;
    private final String ownerLogin;
    private AuthResult latestAuthResult;

    /**
     * Создает новый серверный контекст.
     *
     * @param collectionManager менеджер коллекции
     * @param movieRepository репозиторий фильмов
     * @param userRepository репозиторий пользователей
     * @param passwordHasher хешер паролей
     * @param ownersByKey владельцы элементов по ключам
     * @param ownerLogin логин владельца для команд без авторизации
     */
    public ServerContext(
        CollectionManager collectionManager,
        MovieRepository movieRepository,
        UserRepository userRepository,
        PasswordHasher passwordHasher,
        Map<String, String> ownersByKey,
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
        this.userRepository = Objects.requireNonNull(
            userRepository,
            "User repository cannot be null"
        );
        this.passwordHasher = Objects.requireNonNull(
            passwordHasher,
            "Password hasher cannot be null"
        );
        this.ownersByKey = Objects.requireNonNull(
            ownersByKey,
            "Owners map cannot be null"
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

    @Override
    public Map<String, Movie> visibleCollection() {
        return collectionManager.getCollection();
    }

    /**
     * Возвращает репозиторий фильмов.
     *
     * @return репозиторий фильмов
     */
    public MovieRepository movieRepository() {
        return movieRepository;
    }

    /**
     * Возвращает репозиторий пользователей.
     *
     * @return репозиторий пользователей
     */
    public UserRepository userRepository() {
        return userRepository;
    }

    /**
     * Возвращает хешер паролей.
     *
     * @return хешер паролей
     */
    public PasswordHasher passwordHasher() {
        return passwordHasher;
    }

    /**
     * Возвращает владельцев элементов по ключам.
     *
     * @return карта владельцев
     */
    public Map<String, String> ownersByKey() {
        return ownersByKey;
    }

    @Override
    public void persistInsertedMovie(String key, Movie movie) throws Exception {
        movieRepository.insert(key, ownerLogin, movie);
        ownersByKey.put(key, ownerLogin);
    }

    @Override
    public boolean persistUpdatedMovie(String key, Movie movie) throws Exception {
        return movieRepository.updateByKey(key, ownerLogin, movie);
    }

    @Override
    public boolean persistRemovedMovie(String key) throws Exception {
        boolean removed = movieRepository.removeByKey(key, ownerLogin);
        if (removed) {
            ownersByKey.remove(key);
        }
        return removed;
    }

    @Override
    public Collection<String> persistRemovedMovies(Collection<String> keys)
        throws Exception {
        Collection<String> removedKeys = movieRepository.removeByKeys(
            keys,
            ownerLogin
        );
        removedKeys.forEach(ownersByKey::remove);
        return removedKeys;
    }

    @Override
    public Collection<String> persistClearedCollection() throws Exception {
        Collection<String> removedKeys = movieRepository.clear(ownerLogin);
        removedKeys.forEach(ownersByKey::remove);
        return removedKeys;
    }

    @Override
    public AuthResult register(String login, String password) throws Exception {
        String passwordHash = passwordHasher.hash(password);
        if (!userRepository.create(login, passwordHash)) {
            throw new IllegalArgumentException(
                "user with login \"" + login + "\" already exists"
            );
        }
        latestAuthResult = new AuthResult(login);
        return latestAuthResult;
    }

    @Override
    public AuthResult login(String login, String password) throws Exception {
        String storedHash = userRepository.findPasswordHash(login)
            .orElseThrow(() -> new IllegalArgumentException("invalid login or password"));
        if (!passwordHasher.verify(password, storedHash)) {
            throw new IllegalArgumentException("invalid login or password");
        }
        latestAuthResult = new AuthResult(login);
        return latestAuthResult;
    }

    @Override
    public AuthResult latestAuthResult() {
        return latestAuthResult;
    }
}
