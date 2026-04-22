package org.commands;

/**
 * Базовый класс для клиентских команд.
 */
public abstract class ClientCommand
    implements Command, ExecutableWithContext<ClientContext> {}
