package org.commands;

import java.rmi.NoSuchObjectException;
import java.util.Objects;
import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;

/**
 * Фабрика для создания команд из пользовательского ввода.
 */
public class CommandFactory {

    private final InputParser inputParser;

    /**
     * Создает фабрику команд.
     *
     * @param inputParser парсер ввода для команд, которым нужен объект Movie
     */
    public CommandFactory(InputParser inputParser) {
        this.inputParser = Objects.requireNonNull(
            inputParser,
            "Input parser cannot be null"
        );
    }

    /**
     * Создает объект команды по имени и строковым аргументам.
     *
     * @param name имя команды
     * @param args аргументы команды
     * @return объект команды
     * @throws Exception если команда не существует или аргументы некорректны
     */
    public Command create(String name, String... args) throws Exception {
        return switch (name) {
            case "help" -> {
                requireArgCount(name, args, 0);
                yield new HelpCommand();
            }
            case "info" -> {
                requireArgCount(name, args, 0);
                yield new InfoCommand();
            }
            case "show" -> {
                requireArgCount(name, args, 0);
                yield new ShowCommand();
            }
            case "insert" -> {
                requireArgCount(name, args, 1);
                yield new InsertCommand(args[0], parseMovie());
            }
            case "update" -> {
                requireArgCount(name, args, 1);
                yield new UpdateCommand(parseId(args[0]), parseMovie());
            }
            case "exit" -> {
                requireArgCount(name, args, 0);
                yield new ExitCommand();
            }
            case "connect" -> {
                requireArgCount(name, args, 2);
                yield new ConnectCommand(args[0], parsePort(args[1]));
            }
            case "use_server" -> {
                requireArgCount(name, args, 2);
                yield new UseServerCommand(args[0], parsePort(args[1]));
            }
            case "servers" -> {
                requireArgCount(name, args, 0);
                yield new ServersCommand();
            }
            case "register" -> {
                requireArgCount(name, args, 2);
                yield new RegisterCommand(args[0], args[1]);
            }
            case "login" -> {
                requireArgCount(name, args, 2);
                yield new LoginCommand(args[0], args[1]);
            }
            case "clear" -> {
                requireArgCount(name, args, 0);
                yield new ClearCommand();
            }
            case "remove_key" -> {
                requireArgCount(name, args, 1);
                yield new RemoveKeyCommand(args[0]);
            }
            case "replace_if_lower" -> {
                requireArgCount(name, args, 1);
                yield new ReplaceIfLowerCommand(args[0], parseMovie());
            }
            case "remove_lower_key" -> {
                requireArgCount(name, args, 1);
                yield new RemoveLowerKeyCommand(args[0]);
            }
            case "remove_greater_key" -> {
                requireArgCount(name, args, 1);
                yield new RemoveGreaterKeyCommand(args[0]);
            }
            case "filter_by_genre" -> {
                requireArgCount(name, args, 1);
                yield new FilterByGenreCommand(MovieGenre.valueOf(args[0]));
            }
            case "filter_contains_name" -> {
                requireArgCount(name, args, 1);
                yield new FilterContainsNameCommand(args[0]);
            }
            case "execute_script" -> {
                requireArgCount(name, args, 1);
                yield new ExecuteScriptCommand(args[0]);
            }
            case "filter_less_than_mpaa_rating" -> {
                requireArgCount(name, args, 1);
                yield new FilterLessThanMpaaRatingCommand(
                    MpaaRating.valueOf(args[0])
                );
            }
            default -> throw new NoSuchObjectException(
                "No command with name \"" + name + "\" exists"
            );
        };
    }

    private void requireArgCount(String commandName, String[] args, int count) {
        if (args.length != count) {
            throw new IllegalArgumentException(
                "command \"" +
                commandName +
                "\" accepts exactly " +
                count +
                " argument" +
                (count == 1 ? "" : "s")
            );
        }
    }

    private int parseId(String rawId) {
        return parseInt(rawId, "id");
    }

    private int parsePort(String rawPort) {
        int port = parseInt(rawPort, "port");
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be in 1..65535");
        }
        return port;
    }

    private int parseInt(String rawValue, String label) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "\"" + rawValue + "\" is not a valid " + label
            );
        }
    }

    private Movie parseMovie() throws Exception {
        return inputParser.parseObject(new Movie());
    }
}
