package org.commands;

import java.io.Serializable;

/**
 * Command sent by a client and executed by a server.
 */
public abstract class SharedCommand
    implements Command, ExecutableWithContext<SharedCommandContext>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Returns whether this command requires an authenticated request.
     *
     * @return true if authentication is required
     */
    public boolean requiresAuthentication() {
        return true;
    }

    /**
     * Returns whether this command only reads server state.
     *
     * @return true if command is read-only
     */
    public boolean isReadOnly() {
        return false;
    }
}
