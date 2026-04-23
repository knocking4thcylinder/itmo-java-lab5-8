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

    public enum Environment {
        CLIENT,
        SERVER,
    }

    private final InputParser inputParser;
    private final Environment environment;

    /**
     * Создает фабрику команд.
     *
     * @param inputParser парсер ввода для команд, которым нужен объект Movie
     */
    public CommandFactory(InputParser inputParser, Environment environment) {
        this.inputParser = Objects.requireNonNull(
            inputParser,
            "Input parser cannot be null"
        );
        this.environment = Objects.requireNonNull(
            environment,
            "Environment cannot be null"
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
                yield environment == Environment.SERVER
                    ? new ServerHelpCommand()
                    : new HelpCommand();
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
                ensureEnvironment(name, Environment.CLIENT);
                requireArgCount(name, args, 1);
                yield new ExecuteScriptCommand(args[0]);
            }
            case "save" -> {
                ensureEnvironment(name, Environment.SERVER);
                requireArgCount(name, args, 0);
                yield new SaveCommand();
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

    private void ensureEnvironment(String commandName, Environment expected) {
        if (environment != expected) {
            throw new IllegalArgumentException(
                "command \"" + commandName + "\" is not available in " +
                environment.name().toLowerCase() +
                " mode"
            );
        }
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
        try {
            return Integer.parseInt(rawId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "\"" + rawId + "\" is not a valid id"
            );
        }
    }

    private Movie parseMovie() throws Exception {
        return inputParser.parseObject(new Movie());
    }
}
