package org.shared;

import java.io.Serializable;
import java.util.Objects;
import org.commands.ServerCommand;

/**
 * Сериализуемый запрос клиента к серверу.
 *
 * @param command серверная команда для выполнения
 */
public record CommandRequest(ServerCommand command) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новый запрос команды.
     *
     * @throws NullPointerException если команда равна null
     */
    public CommandRequest {
        Objects.requireNonNull(command, "Server command cannot be null");
    }

    /**
     * Создает новый запрос на выполнение серверной команды.
     *
     * @param command серверная команда
     * @return запрос команды
     */
    public static CommandRequest of(ServerCommand command) {
        return new CommandRequest(command);
    }
}
