package org.commands;

import java.util.Collection;
import org.CollectionManager;
import org.dataclasses.Movie;

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
    default void persistUpdatedMovie(String key, Movie movie) throws Exception {}

    /**
     * Persists removal of one movie.
     *
     * @param key movie key
     * @throws Exception if persistence fails
     */
    default void persistRemovedMovie(String key) throws Exception {}

    /**
     * Persists removal of several movies.
     *
     * @param keys movie keys
     * @throws Exception if persistence fails
     */
    default void persistRemovedMovies(Collection<String> keys) throws Exception {}

    /**
     * Persists collection clearing.
     *
     * @throws Exception if persistence fails
     */
    default void persistClearedCollection() throws Exception {}
}
