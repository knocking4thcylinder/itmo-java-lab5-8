package org.commands;

import java.io.Serializable;

/**
 * Command sent by a client and executed by a server.
 */
public abstract class SharedCommand
    implements Command, ExecutableWithContext<SharedCommandContext>, Serializable {

    private static final long serialVersionUID = 1L;
}
