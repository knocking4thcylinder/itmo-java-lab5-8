package org.commands;

import java.util.Collection;
import org.App;
import org.dataclasses.Movie;

public class InfoCommand implements ExecutableInterface {

    public static void exec(String... args) {
        Collection<Movie> collection = App.getCollection().values();
        for (Movie movie : collection) {
            System.out.println(movie.toString());
        }
    }
}
