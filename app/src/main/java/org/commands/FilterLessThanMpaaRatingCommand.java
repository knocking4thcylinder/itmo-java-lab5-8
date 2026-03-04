package org.commands;

import org.App;
import org.dataclasses.Movie;
import org.dataclasses.enums.MpaaRating;

public class FilterLessThanMpaaRatingCommand implements ExecutableInterface {

    public static void exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"filter_less_than_mpaa_rating\" accepts exactly one argument"
            );
        }
        for (Movie movie : App.getCollection().values()) {
            if (
                movie.getMpaaRating().compareTo(MpaaRating.valueOf(args[0])) < 0
            ) {
                System.out.println(movie);
            }
        }
    }
}
