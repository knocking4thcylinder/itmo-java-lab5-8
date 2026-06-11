package org.commands;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import org.CollectionManager;
import org.dataclasses.Movie;
import org.shared.AuthResult;

/**
 * Context visible to commands shared by client and server.
 */
public interface SharedCommandContext {

    /**
     * Returns the collection manager used by shared commands.
     *
     * @return collection manager
     */
    CollectionManager collectionManager();

    /**
     * Returns the part of the collection visible to the current command.
     *
     * @return visible collection entries
     */
    default Map<String, Movie> visibleCollection() {
        return collectionManager().getCollection();
    }

    /**
     * Persists a newly inserted movie when the context is backed by storage.
     *
     * @param key movie key
     * @param movie movie to persist
     * @throws Exception if persistence fails
     */
    default void persistInsertedMovie(String key, Movie movie) throws Exception {}

    /**
     * Persists changed movie fields for an existing key.
     *
     * @param key movie key
     * @param movie movie state to persist
     * @throws Exception if persistence fails
     */
    default boolean persistUpdatedMovie(String key, Movie movie) throws Exception {
        return true;
    }

    /**
     * Persists removal of one movie.
     *
     * @param key movie key
     * @throws Exception if persistence fails
     */
    default boolean persistRemovedMovie(String key) throws Exception {
        return true;
    }

    /**
     * Persists removal of several movies.
     *
     * @param keys movie keys
     * @throws Exception if persistence fails
     */
    default Collection<String> persistRemovedMovies(Collection<String> keys)
        throws Exception {
        return keys;
    }

    /**
     * Persists collection clearing.
     *
     * @throws Exception if persistence fails
     */
    default Collection<String> persistClearedCollection() throws Exception {
        return new ArrayList<>(collectionManager().getCollection().keySet());
    }

    /**
     * Registers a new user and creates an authenticated session.
     *
     * @param login user login
     * @param password plain password
     * @return authentication result
     * @throws Exception if registration fails
     */
    default AuthResult register(String login, String password) throws Exception {
        throw new UnsupportedOperationException("Registration is not supported here");
    }

    /**
     * Authenticates an existing user and creates a session.
     *
     * @param login user login
     * @param password plain password
     * @return authentication result
     * @throws Exception if authentication fails
     */
    default AuthResult login(String login, String password) throws Exception {
        throw new UnsupportedOperationException("Login is not supported here");
    }

    /**
     * Returns auth result produced by the last command in this context.
     *
     * @return auth result or null
     */
    default AuthResult latestAuthResult() {
        return null;
    }

    /**
     * Returns the owner login for the current command.
     *
     * @return owner login or null when ownership is not available
     */
    default String ownerLogin() {
        return null;
    }

    /**
     * Returns the owner login for a collection key.
     *
     * @param key collection key
     * @return owner login or null when the key has no known owner
     */
    default String ownerOf(String key) {
        return null;
    }
}
