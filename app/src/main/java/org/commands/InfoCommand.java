package org.commands;

import java.util.Collection;
import org.App;
import org.dataclasses.Movie;

public class InfoCommand implements ExecutableInterface {

    public static void exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"info\" does not accept any arguments"
            );
        }
        Collection<Movie> collection = App.getCollection().values();
        for (Movie movie : collection) {
            System.out.println(movie.toString());
        }
    }
}
