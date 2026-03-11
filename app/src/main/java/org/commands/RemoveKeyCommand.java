package org.commands;

import org.CollectionManager;

public class RemoveKeyCommand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"remove_key\" accepts exactly one argument"
            );
        }
        String key = args[0];
        if (!CollectionManager.getInstance().containsKey(key)) {
            return "no element with key " + key + " exists in the collection";
        }
        CollectionManager.getInstance().remove(key);
        return "removed element with key " + key;
    }
}
