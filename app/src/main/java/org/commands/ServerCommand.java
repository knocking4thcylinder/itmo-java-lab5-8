package org.commands;

import java.io.Serializable;

/**
 * Базовый класс для серверных команд.
 */
public abstract class ServerCommand implements Executable, Serializable {

    private static final long serialVersionUID = 1L;
}
