package org.commands;

import org.App;
import org.dataclasses.Movie;

public class UpdateCommand implements Executable {

    public static void exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"update\" accepts exactly one argument"
            );
        }

        int id;
        try {
            id = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("\"" + args[0] + "\" is not a valid id");
            return;
        }
        var collection = App.getCollection();
        InputParser inputParser = App.getInputParser();
        for (var entry : collection.entrySet()) {
            if (entry.getValue().getId() == id) {
                Movie movie = inputParser.parseObject(entry.getValue());
                collection.put(entry.getKey(), movie);
                System.out.println(
                    "element " + entry.getKey() + " successfully updated"
                );
                return;
            }
        }
        System.out.println("No elment with that id exists");
    }
}
