package org.commands;

import org.App;
import org.dataclasses.Movie;

public class ReplaceIfLowerCommand implements Executable {

    @Override
    public String exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"replace_if_lower\" accepts exactly one argument"
            );
        }

        String key = args[0];
        var collection = App.getCollection();
        if (!collection.containsKey(key)) {
            return "no element with key " + key + " exists in the collection";
        }
        InputParser inputParser = App.getInputParser();
        Movie movie = new Movie();
        movie = inputParser.parseObject(movie);
        if (movie.compareTo(App.getCollection().get(key)) < 0) {
            collection.put(key, movie);
            return "element " + key + " successfully updated";
        }
        return "element " + key + " was not replaced (new value is not lower)";
    }
}
