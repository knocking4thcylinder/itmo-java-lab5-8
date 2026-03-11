package org.commands;

import org.App;

public class RemoveKeyCommand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"remove_key\" accepts exactly one argument"
            );
        }
        String key = args[0];
        if (!App.getCollection().keySet().contains(key)) {
            return "no element with key " + key + " exists in the collection";
        }
        App.getCollection().remove(key);
        return "removed element with key " + key;
    }
}
