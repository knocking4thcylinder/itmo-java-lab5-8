package org.commands;

import org.CollectionManager;

public class ClearComand implements Executable {

    @Override
    public String exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"clear\" does not accept any arguments"
            );
        }

        int collectionLength = CollectionManager.getInstance().size();
        CollectionManager.getInstance().clear();
        return "collection cleared, removed " + collectionLength + " elements";
    }
}
