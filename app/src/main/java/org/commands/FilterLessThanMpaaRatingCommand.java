package org.commands;

import org.CollectionManager;
import org.dataclasses.Movie;
import org.dataclasses.enums.MpaaRating;

public class FilterLessThanMpaaRatingCommand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"filter_less_than_mpaa_rating\" accepts exactly one argument"
            );
        }
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            if (
                movie.getMpaaRating().compareTo(MpaaRating.valueOf(args[0])) < 0
            ) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
