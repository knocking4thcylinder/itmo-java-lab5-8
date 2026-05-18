package org.commands;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
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
    private final Map<ServerEndpoint, ServerSession> sessions;
    private ServerEndpoint activeEndpoint;

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
            Collections.synchronizedSet(new HashSet<>()),
            new LinkedHashMap<>(),
            null
        );
    }

    private ClientContext(
        InputParser inputParser,
        CommandFactory commandFactory,
        ClientCommandInvoker commandInvoker,
        Set<String> executingScripts,
        Map<ServerEndpoint, ServerSession> sessions,
        ServerEndpoint activeEndpoint
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
        this.sessions = Objects.requireNonNull(sessions, "Sessions cannot be null");
        this.activeEndpoint = activeEndpoint;
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
     * Возвращает логин текущего пользователя.
     *
     * @return логин или null
     */
    public String login() {
        ServerSession session = activeSessionOrNull();
        return session == null ? null : session.login();
    }

    /**
     * Возвращает пароль текущего пользователя.
     *
     * @return пароль или null
     */
    public String password() {
        ServerSession session = activeSessionOrNull();
        return session == null ? null : session.password();
    }

    /**
     * Сохраняет состояние авторизации клиента.
     *
     * @param login логин пользователя
     * @param password пароль
     */
    public void authenticate(String login, String password) {
        authenticate(activeEndpoint(), login, password);
    }

    /**
     * Saves authentication state for a server.
     *
     * @param endpoint server endpoint
     * @param login login
     * @param password password
     */
    public void authenticate(
        ServerEndpoint endpoint,
        String login,
        String password
    ) {
        session(endpoint).authenticate(login, password);
    }

    /**
     * Сбрасывает состояние авторизации клиента.
     */
    public void clearAuthentication() {
        ServerSession session = activeSessionOrNull();
        if (session != null) {
            session.clearAuthentication();
        }
    }

    /**
     * Adds a server and makes it active.
     *
     * @param endpoint server endpoint
     */
    public void connect(ServerEndpoint endpoint) {
        session(endpoint);
        activeEndpoint = endpoint;
    }

    /**
     * Changes the active server.
     *
     * @param endpoint server endpoint
     */
    public void useServer(ServerEndpoint endpoint) {
        if (!sessions.containsKey(endpoint)) {
            throw new IllegalArgumentException(
                "Server " + endpoint + " is not connected"
            );
        }
        activeEndpoint = endpoint;
    }

    /**
     * Returns active server endpoint.
     *
     * @return active endpoint
     */
    public ServerEndpoint activeEndpoint() {
        if (activeEndpoint == null) {
            throw new IllegalStateException("No active server. Use connect first.");
        }
        return activeEndpoint;
    }

    /**
     * Returns connected server sessions.
     *
     * @return sessions by endpoint
     */
    public Map<ServerEndpoint, ServerSession> sessions() {
        return Collections.unmodifiableMap(sessions);
    }

    public ServerSession session(ServerEndpoint endpoint) {
        return sessions.computeIfAbsent(endpoint, ServerSession::new);
    }

    private ServerSession activeSessionOrNull() {
        if (activeEndpoint == null) {
            return null;
        }
        return sessions.get(activeEndpoint);
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
        ClientContext copiedContext = new ClientContext(
            copiedInputParser,
            new CommandFactory(copiedInputParser),
            commandInvoker,
            executingScripts,
            sessions,
            activeEndpoint
        );
        return copiedContext;
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
