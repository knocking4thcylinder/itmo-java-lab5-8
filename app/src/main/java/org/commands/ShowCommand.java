package org.commands;

import org.CollectionManager;
import org.dataclasses.Movie;

public class ShowCommand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"show\" does not accept any arguments"
            );
        }
        StringBuilder sb = new StringBuilder();
        for (Movie movie : CollectionManager.getInstance()
            .getCollection()
            .values()) {
            sb.append(movie).append("\n");
        }
        return sb.toString();
    }
}
