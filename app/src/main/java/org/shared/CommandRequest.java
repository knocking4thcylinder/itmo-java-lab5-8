package org.shared;

import java.io.Serializable;
import java.util.List;
import org.dataclasses.Movie;

/**
 * Универсальный сериализуемый запрос на выполнение команды.
 *
 * @param commandName имя команды
 * @param arguments аргументы команды
 * @param movie дополнительный объект фильма для команд, которым он нужен
 */
public record CommandRequest(
    String commandName,
    List<String> arguments,
    Movie movie
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новый запрос команды.
     *
     * @throws IllegalArgumentException если имя команды пустое
     * @throws NullPointerException если список аргументов равен null
     */
    public CommandRequest {
        if (commandName == null || commandName.isBlank()) {
            throw new IllegalArgumentException(
                "Command name cannot be null or blank"
            );
        }
        if (arguments == null) {
            throw new NullPointerException("Command arguments cannot be null");
        }
        arguments = List.copyOf(arguments);
    }

    /**
     * Создает запрос без объектной части.
     *
     * @param commandName имя команды
     * @param arguments аргументы команды
     * @return запрос команды
     */
    public static CommandRequest of(String commandName, List<String> arguments) {
        return new CommandRequest(commandName, arguments, null);
    }

    /**
     * Создает запрос без аргументов и объектной части.
     *
     * @param commandName имя команды
     * @return запрос команды
     */
    public static CommandRequest of(String commandName) {
        return new CommandRequest(commandName, List.of(), null);
    }

    /**
     * Возвращает true, если запрос содержит объект фильма.
     *
     * @return true при наличии фильма
     */
    public boolean hasMovie() {
        return movie != null;
    }
}
