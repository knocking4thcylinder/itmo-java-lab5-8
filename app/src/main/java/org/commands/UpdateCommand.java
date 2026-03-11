package org.commands;

import org.App;
import org.dataclasses.Movie;

public class UpdateCommand implements Executable {

    @Override
    public String exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"update\" accepts exactly one argument"
            );
        }

        int id;
        try {
            id = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            return "\"" + args[0] + "\" is not a valid id";
        }
        var collection = App.getCollection();
        InputParser inputParser = App.getInputParser();
        for (var entry : collection.entrySet()) {
            if (entry.getValue().getId() == id) {
                Movie movie = inputParser.parseObject(entry.getValue());
                collection.put(entry.getKey(), movie);
                return "element " + entry.getKey() + " successfully updated";
            }
        }
        return "No element with that id exists";
    }
}
