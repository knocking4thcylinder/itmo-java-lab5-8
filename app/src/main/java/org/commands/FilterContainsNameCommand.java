package org.commands;

import org.App;
import org.dataclasses.Movie;

public class FilterContainsNameCommand implements ExecutableInterface {

    public static void exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"filter_contains_name\" accepts exactly one argument"
            );
        }
        for (Movie movie : App.getCollection().values()) {
            if (movie.getName().contains(args[0])) {
                System.out.println(movie);
            }
        }
    }
}
