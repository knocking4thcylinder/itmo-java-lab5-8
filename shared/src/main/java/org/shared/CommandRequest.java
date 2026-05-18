package org.shared;

import java.io.Serializable;
import java.util.Objects;
import org.commands.SharedCommand;

/**
 * Сериализуемый запрос клиента к серверу.
 *
 * @param command удаленная команда для выполнения на сервере
 * @param login логин пользователя
 * @param password пароль пользователя
 */
public record CommandRequest(
    SharedCommand command,
    String login,
    String password
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
        return new CommandRequest(command, null, null);
    }

    /**
     * Создает новый запрос на выполнение серверной команды.
     *
     * @param command общая команда
     * @param login логин пользователя
     * @param password пароль пользователя
     * @return запрос команды
     */
    public static CommandRequest of(
        SharedCommand command,
        String login,
        String password
    ) {
        return new CommandRequest(command, login, password);
    }
}
