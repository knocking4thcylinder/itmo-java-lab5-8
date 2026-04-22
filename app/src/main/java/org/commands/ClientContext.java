package org.commands;

import java.util.Arrays;
import java.util.Objects;

/**
 * Клиентский контекст выполнения команд.
 */
public class ClientContext {

    private final InputParser inputParser;
    private final CommandFactory commandFactory;
    private final CommandInvoker commandInvoker;
    private final ServerContext serverContext;

    /**
     * Создает новый клиентский контекст.
     *
     * @param inputParser парсер клиентского ввода
     * @param commandFactory фабрика команд
     * @param commandInvoker инвокер команд
     * @param serverContext серверный контекст
     */
    public ClientContext(
        InputParser inputParser,
        CommandFactory commandFactory,
        CommandInvoker commandInvoker,
        ServerContext serverContext
    ) {
        this.inputParser = Objects.requireNonNull(
            inputParser,
            "Input parser cannot be null"
        );
        this.commandFactory = Objects.requireNonNull(
            commandFactory,
            "Command factory cannot be null"
        );
        this.commandInvoker = Objects.requireNonNull(
            commandInvoker,
            "Command invoker cannot be null"
        );
        this.serverContext = Objects.requireNonNull(
            serverContext,
            "Server context cannot be null"
        );
    }

    /**
     * Возвращает парсер клиентского ввода.
     *
     * @return парсер ввода
     */
    public InputParser inputParser() {
        return inputParser;
    }

    /**
     * Возвращает серверный контекст.
     *
     * @return серверный контекст
     */
    public ServerContext serverContext() {
        return serverContext;
    }

    /**
     * Создает и выполняет команду, представленную строковыми частями.
     *
     * @param commandParts имя команды и её аргументы
     * @return результат выполнения
     * @throws Exception при ошибке создания или выполнения
     */
    public String dispatch(String[] commandParts) throws Exception {
        return dispatch(
            commandFactory.create(
                commandParts[0],
                Arrays.copyOfRange(commandParts, 1, commandParts.length)
            )
        );
    }

    /**
     * Выполняет уже созданную команду в корректном контексте.
     *
     * @param command команда для выполнения
     * @return результат выполнения
     * @throws Exception при ошибке выполнения
     */
    public String dispatch(Command command) throws Exception {
        if (command instanceof ClientCommand clientCommand) {
            return commandInvoker.invoke(clientCommand, this);
        }
        if (command instanceof ServerCommand serverCommand) {
            return commandInvoker.invoke(serverCommand, serverContext);
        }
        throw new IllegalStateException(
            "Unsupported command type: " + command.getClass().getName()
        );
    }
}
