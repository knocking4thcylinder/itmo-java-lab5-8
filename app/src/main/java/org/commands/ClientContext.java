package org.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Клиентский контекст выполнения команд.
 */
public class ClientContext {

    private final InputParser inputParser;
    private final CommandFactory commandFactory;
    private final ClientCommandInvoker commandInvoker;
    private final Set<String> executingScripts;

    /**
     * Создает новый клиентский контекст.
     *
     * @param inputParser парсер клиентского ввода
     * @param commandFactory фабрика команд
     * @param commandInvoker диспетчер выполнения команд
     */
    public ClientContext(
        InputParser inputParser,
        CommandFactory commandFactory,
        ClientCommandInvoker commandInvoker
    ) {
        this(
            inputParser,
            commandFactory,
            commandInvoker,
            Collections.synchronizedSet(new HashSet<>())
        );
    }

    private ClientContext(
        InputParser inputParser,
        CommandFactory commandFactory,
        ClientCommandInvoker commandInvoker,
        Set<String> executingScripts
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
        this.executingScripts = Objects.requireNonNull(
            executingScripts,
            "Executing scripts cannot be null"
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
     * Возвращает фабрику команд.
     *
     * @return фабрика команд
     */
    public CommandFactory commandFactory() {
        return commandFactory;
    }

    /**
     * Возвращает диспетчер выполнения команд.
     *
     * @return invoker команд
     */
    public ClientCommandInvoker commandInvoker() {
        return commandInvoker;
    }
    
    /**
     * Создает копию клиентского контекста с другим источником ввода.
     *
     * @param inputSource новый источник ввода для копии
     * @return копия контекста
     */
    public ClientContext copyWithInputSource(InputSource inputSource) {
        InputParser copiedInputParser = new InputParser(
            Objects.requireNonNull(inputSource, "Input source cannot be null")
        );
        return new ClientContext(
            copiedInputParser,
            new CommandFactory(
                copiedInputParser,
                CommandFactory.Environment.CLIENT
            ),
            commandInvoker,
            executingScripts
        );
    }

    /**
     * Проверяет, выполняется ли скрипт с указанным именем.
     *
     * @param fileName имя файла
     * @return true, если скрипт уже выполняется
     */
    public boolean isExecutingScript(String fileName) {
        return executingScripts.contains(fileName);
    }

    /**
     * Помечает скрипт как выполняющийся.
     *
     * @param fileName имя файла
     */
    public void beginScript(String fileName) {
        executingScripts.add(fileName);
    }

    /**
     * Снимает отметку выполнения со скрипта.
     *
     * @param fileName имя файла
     */
    public void endScript(String fileName) {
        executingScripts.remove(fileName);
    }
}
