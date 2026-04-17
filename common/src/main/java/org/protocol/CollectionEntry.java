package org.protocol;

import java.io.Serial;
import java.io.Serializable;
import org.dataclasses.Movie;

public record CollectionEntry(String key, Movie movie) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
