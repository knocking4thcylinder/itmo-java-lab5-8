package org.shared;

import java.io.Serializable;

/**
 * Ответ сервера на выполнение команды.
 *
 * @param success признак успешного выполнения
 * @param message текст результата или ошибки
 */
public record CommandResponse(
    boolean success,
    String message
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
        return new CommandResponse(true, message);
    }

    /**
     * Ответ сервера с ошибкой.
     *
     * @param message текст ошибки
     * @return неуспешный ответ
     */
    public static CommandResponse failure(String message) {
        return new CommandResponse(false, message);
    }
}
