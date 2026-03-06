package org.commands;

import org.App;

public class RemoveKeyCommand implements Executable {

    public static void exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"remove_key\" accepts exactly one argument"
            );
        }
        String key = args[0];
        if (!App.getCollection().keySet().contains(key)) {
            System.out.println(
                "no element with key " + key + " exists in the collection"
            );
            return;
        }
        App.getCollection().remove(key);
        System.out.println("removed element with key " + key);
    }
}
