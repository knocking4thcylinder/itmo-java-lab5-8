package org.commands;

import org.CollectionManager;

/**
 * Context visible to commands shared by client and server.
 */
public interface SharedCommandContext {

    /**
     * Returns the collection manager used by shared commands.
     *
     * @return collection manager
     */
    CollectionManager collectionManager();
}
