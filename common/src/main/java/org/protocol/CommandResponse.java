package org.protocol;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public final class CommandResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final List<CollectionEntry> collectionEntries;

    private CommandResponse(
        boolean success,
        String message,
        List<CollectionEntry> collectionEntries
    ) {
        this.success = success;
        this.message = message;
        this.collectionEntries = List.copyOf(collectionEntries);
    }

    public static CommandResponse success(String message) {
        return new CommandResponse(true, message, List.of());
    }

    public static CommandResponse success(
        String message,
        List<CollectionEntry> collectionEntries
    ) {
        return new CommandResponse(true, message, collectionEntries);
    }

    public static CommandResponse failure(String message) {
        return new CommandResponse(false, message, List.of());
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<CollectionEntry> getCollectionEntries() {
        return collectionEntries;
    }
}
