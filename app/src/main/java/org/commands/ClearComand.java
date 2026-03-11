package org.commands;

import org.App;

public class ClearComand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"clear\" does not accept any arguments"
            );
        }

        int collectionLength = App.getCollection().values().toArray().length;
        App.getCollection().clear();
        return "collection cleared, removed " + collectionLength + " elements";
    }
}
