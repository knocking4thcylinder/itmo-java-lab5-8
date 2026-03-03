package org.commands;

import org.App;

public class ClearComand implements ExecutableInterface {

    public static void exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"clear\" does not accept any arguments"
            );
        }

        int collectionLength = App.getCollection().values().toArray().length;
        App.getCollection().clear();
        System.out.println(
            "collection cleared, removed " + collectionLength + " elements"
        );
    }
}
