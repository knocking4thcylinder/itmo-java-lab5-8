package org.commands;

import org.App;
import org.dataclasses.Movie;

public class ReplaceIfLowerCommand implements Executable {

    public static void exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"repalce_if_lower\" accepts exactly one argument"
            );
        }

        String key = args[0];
        var collection = App.getCollection();
        InputParser inputParser = App.getInputParser();
        Movie movie = new Movie();
        movie = inputParser.parseObject(movie);
        if (movie.compareTo(App.getCollection().get(key)) < 0) {
            collection.put(key, movie);
            System.out.println("element " + key + " successfully updated");
        }
    }
}
