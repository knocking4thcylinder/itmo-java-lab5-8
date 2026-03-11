package org.commands;

import org.CollectionManager;
import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;

public class FilterByGenreCommand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"filter_by_genre\" accepts exactly one argument"
            );
        }
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            if (
                movie.getGenre() != null &&
                movie.getGenre().equals(MovieGenre.valueOf(args[0]))
            ) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
