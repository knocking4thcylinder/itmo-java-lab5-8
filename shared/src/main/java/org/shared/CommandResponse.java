package org.shared;

import java.io.Serializable;

/**
 * Ответ сервера на выполнение команды.
 *
 * @param success признак успешного выполнения
 * @param message текст результата или ошибки
 * @param login логин авторизованного пользователя
 */
public record CommandResponse(
    boolean success,
    String message,
    String login
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новый ответ команды.
     *
     * @throws NullPointerException если сообщение равно null
     */
    public CommandResponse {
        if (message == null) {
            throw new NullPointerException("Response message cannot be null");
        }
    }

    /**
     * Успешный ответ сервера.
     *
     * @param message текст результата
     * @return успешный ответ
     */
    public static CommandResponse success(String message) {
        return new CommandResponse(true, message, null);
    }

    /**
     * Успешный ответ сервера с данными авторизации.
     *
     * @param message текст результата
     * @param login логин пользователя
     * @return успешный ответ
     */
    public static CommandResponse authenticated(
        String message,
        String login
    ) {
        return new CommandResponse(true, message, login);
    }

    /**
     * Ответ сервера с ошибкой.
     *
     * @param message текст ошибки
     * @return неуспешный ответ
     */
    public static CommandResponse failure(String message) {
        return new CommandResponse(false, message, null);
    }
}
