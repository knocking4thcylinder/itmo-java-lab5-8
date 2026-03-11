package org.commands;

import org.App;
import org.dataclasses.Movie;

public class FilterContainsNameCommand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"filter_contains_name\" accepts exactly one argument"
            );
        }
        StringBuilder sb = new StringBuilder();
        for (Movie movie : App.getCollection().values()) {
            if (movie.getName() != null && movie.getName().contains(args[0])) {
                sb.append(movie).append("\n");
            }
        }
        return sb.toString();
    }
}
