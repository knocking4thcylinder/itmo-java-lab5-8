package org.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.NoSuchObjectException;
import java.util.HashSet;
import java.util.Set;
import org.client.io.InputParser;
import org.client.io.ScannerInputSource;
import org.protocol.CollectionEntry;
import org.protocol.CommandRequest;
import org.protocol.CommandResponse;
import org.protocol.CommandType;
import org.protocol.MovieData;

public final class ClientCommandRunner {

    private static final String CLIENT_HELP = """
        help : вывести справку по доступным командам
        info : вывести информацию о коллекции
        show : вывести все элементы коллекции
        insert key {element} : добавить новый элемент с заданным ключом
        update id {element} : обновить элемент по id
        clear : очистить коллекцию
        remove_key key : удалить элемент по ключу
        replace_if_lower key {element} : заменить значение по ключу, если новое значение меньше старого
        remove_greater_key key : удалить элементы с ключом больше заданного
        remove_lower_key key : удалить элементы с ключом меньше заданного
        filter_by_genre genre : вывести элементы с заданным жанром
        filter_contains_name name : вывести элементы, имя которых содержит подстроку
        filter_less_than_mpaa_rating mpaaRating : вывести элементы с рейтингом меньше заданного
        execute_script file_name : считать и исполнить скрипт из указанного файла
        exit : завершить клиент
        """;

    private final String host;
    private final int port;
    private final InputParser inputParser;
    private final NonBlockingRequestClient requestClient;
    private final Set<Path> executingScripts = new HashSet<>();

    public ClientCommandRunner(
        String host,
        int port,
        InputParser inputParser,
        NonBlockingRequestClient requestClient
    ) {
        this.host = host;
        this.port = port;
        this.inputParser = inputParser;
        this.requestClient = requestClient;
    }

    public void runInteractive() throws Exception {
        System.out.print("> ");
        while (inputParser.hasNextLine()) {
            boolean shouldContinue;
            try {
                shouldContinue = executeCommand(
                    inputParser.parseCommand(),
                    inputParser
                );
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
                shouldContinue = true;
            }
            if (!shouldContinue) {
                return;
            }
            System.out.print("> ");
        }
    }

    private boolean executeCommand(String[] command, InputParser parser)
        throws Exception {
        if (command.length == 0 || command[0].isBlank()) {
            return true;
        }

        return switch (command[0]) {
            case "help" -> {
                System.out.println(CLIENT_HELP);
                yield true;
            }
            case "exit" -> false;
            case "save" -> {
                System.out.println("The save command is only available on the server.");
                yield true;
            }
            case "execute_script" -> {
                yield executeScript(command);
            }
            default -> {
                CommandRequest request = toRequest(command, parser);
                CommandResponse response = requestClient.send(host, port, request);
                printResponse(response);
                yield true;
            }
        };
    }

    private boolean executeScript(String[] command) throws Exception {
        if (command.length != 2) {
            throw new IllegalArgumentException(
                "command \"execute_script\" accepts exactly one argument"
            );
        }

        Path scriptPath = Path.of(command[1]).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IOException("No script found on path " + scriptPath);
        }
        if (!executingScripts.add(scriptPath)) {
            throw new IllegalStateException(
                "Cannot execute script recursively: " + scriptPath
            );
        }

        try (
            InputParser scriptParser = new InputParser(
                new ScannerInputSource(Files.newInputStream(scriptPath))
            )
        ) {
            while (scriptParser.hasNextLine()) {
                boolean shouldContinue;
                try {
                    shouldContinue = executeCommand(
                        scriptParser.parseCommand(),
                        scriptParser
                    );
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                    shouldContinue = true;
                }
                if (!shouldContinue) {
                    return false;
                }
            }
        } finally {
            executingScripts.remove(scriptPath);
        }
        return true;
    }

    private CommandRequest toRequest(String[] command, InputParser parser)
        throws Exception {
        return switch (command[0]) {
            case "info" -> requireNoArguments(command, CommandRequest.of(CommandType.INFO));
            case "show" -> requireNoArguments(command, CommandRequest.of(CommandType.SHOW));
            case "clear" -> requireNoArguments(command, CommandRequest.of(CommandType.CLEAR));
            case "insert" -> CommandRequest.withMovie(
                CommandType.INSERT,
                requireSingleArgument(command, "insert"),
                parser.parseObject(new MovieData())
            );
            case "update" -> CommandRequest.withMovie(
                CommandType.UPDATE,
                parseIntegerArgument(requireSingleArgument(command, "update")),
                parser.parseObject(new MovieData())
            );
            case "remove_key" -> CommandRequest.withString(
                CommandType.REMOVE_KEY,
                requireSingleArgument(command, "remove_key")
            );
            case "replace_if_lower" -> CommandRequest.withMovie(
                CommandType.REPLACE_IF_LOWER,
                requireSingleArgument(command, "replace_if_lower"),
                parser.parseObject(new MovieData())
            );
            case "remove_lower_key" -> CommandRequest.withString(
                CommandType.REMOVE_LOWER_KEY,
                requireSingleArgument(command, "remove_lower_key")
            );
            case "remove_greater_key" -> CommandRequest.withString(
                CommandType.REMOVE_GREATER_KEY,
                requireSingleArgument(command, "remove_greater_key")
            );
            case "filter_by_genre" -> CommandRequest.withString(
                CommandType.FILTER_BY_GENRE,
                requireSingleArgument(command, "filter_by_genre")
            );
            case "filter_contains_name" -> CommandRequest.withString(
                CommandType.FILTER_CONTAINS_NAME,
                requireSingleArgument(command, "filter_contains_name")
            );
            case "filter_less_than_mpaa_rating" -> CommandRequest.withString(
                CommandType.FILTER_LESS_THAN_MPAA_RATING,
                requireSingleArgument(command, "filter_less_than_mpaa_rating")
            );
            default -> throw new NoSuchObjectException(
                "No command with name \"" + command[0] + "\" exists"
            );
        };
    }

    private String requireSingleArgument(String[] command, String commandName) {
        if (command.length != 2) {
            throw new IllegalArgumentException(
                "command \"" + commandName + "\" accepts exactly one argument"
            );
        }
        return command[1];
    }

    private CommandRequest requireNoArguments(
        String[] command,
        CommandRequest request
    ) {
        if (command.length != 1) {
            throw new IllegalArgumentException(
                "command \"" + command[0] + "\" does not accept any arguments"
            );
        }
        return request;
    }

    private Integer parseIntegerArgument(String argument) {
        try {
            return Integer.valueOf(argument);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                "\"" + argument + "\" is not a valid id"
            );
        }
    }

    private void printResponse(CommandResponse response) {
        if (response.getMessage() != null && !response.getMessage().isBlank()) {
            System.out.println(response.getMessage());
        }
        for (CollectionEntry entry : response.getCollectionEntries()) {
            System.out.println(entry.key() + " => " + entry.movie());
        }
    }
}
