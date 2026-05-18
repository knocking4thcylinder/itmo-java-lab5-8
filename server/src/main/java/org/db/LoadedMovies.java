package org.db;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.dataclasses.Movie;

/**
 * Movies loaded from storage with server-side ownership metadata.
 *
 * @param movies movies by collection key
 * @param ownersByKey owner login by collection key
 */
public record LoadedMovies(
    TreeMap<String, Movie> movies,
    Map<String, String> ownersByKey
) {

    public LoadedMovies {
        Objects.requireNonNull(movies, "Movies cannot be null");
        Objects.requireNonNull(ownersByKey, "Owners map cannot be null");
    }
}
