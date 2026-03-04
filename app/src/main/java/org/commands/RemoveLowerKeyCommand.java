package org.commands;

import org.App;

public class RemoveLowerKeyCommand implements ExecutableInterface {

    public static void exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"remove_lower_key\" accepts exactly one argument"
            );
        }
        String key = args[0];
        var collection = App.getCollection();
        for (String k : collection.keySet()) {
            if (k.compareTo(key) < 0) {
                collection.remove(k);
            }
        }
    }
}
