package org.commands;

import org.App;

public class InfoCommand implements Executable {

    public static void exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"info\" does not accept any arguments"
            );
        }
        var collection = App.getCollection().entrySet();
        for (var movie : collection) {
            System.out.println(movie.getKey() + " -> " + movie.getValue());
        }
    }
}
