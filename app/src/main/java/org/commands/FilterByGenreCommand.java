package org.commands;

import org.App;
import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;

public class FilterByGenreCommand implements Executable {

    public static void exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"filter_by_genre\" accepts exactly one argument"
            );
        }
        for (Movie movie : App.getCollection().values()) {
            if (movie.getGenre().equals(MovieGenre.valueOf(args[0]))) {
                System.out.println(movie);
            }
        }
    }
}
