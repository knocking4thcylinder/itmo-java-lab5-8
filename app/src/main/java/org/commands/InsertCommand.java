package org.commands;

import org.App;
import org.dataclasses.Movie;

public class InsertCommand implements Executable {

    @Override
    public String exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"insert\" accepts exactly one argument"
            );
        }
        String key = args[0];
        var collection = App.getCollection();
        InputParser inputParser = App.getInputParser();
        Movie movie = new Movie();
        movie = inputParser.parseObject(movie);
        collection.put(key, movie);
        return "element " + key + " successfully inserted";
    }
}
