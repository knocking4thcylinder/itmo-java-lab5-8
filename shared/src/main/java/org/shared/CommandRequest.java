package org.shared;

import java.io.Serializable;
import java.util.Objects;
import org.commands.SharedCommand;

/**
 * Сериализуемый запрос клиента к серверу.
 *
 * @param command удаленная команда для выполнения на сервере
 * @param authToken токен авторизации клиента
 */
public record CommandRequest(
    SharedCommand command,
    String authToken
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новый запрос команды.
     *
     * @throws NullPointerException если команда равна null
     */
    public CommandRequest {
        Objects.requireNonNull(command, "Shared command cannot be null");
    }

    /**
     * Создает новый запрос на выполнение серверной команды.
     *
     * @param command общая команда
     * @return запрос команды
     */
    public static CommandRequest of(SharedCommand command) {
        return new CommandRequest(command, null);
    }

    /**
     * Создает новый запрос на выполнение серверной команды.
     *
     * @param command общая команда
     * @param authToken токен авторизации клиента
     * @return запрос команды
     */
    public static CommandRequest of(SharedCommand command, String authToken) {
        return new CommandRequest(command, authToken);
    }
}
