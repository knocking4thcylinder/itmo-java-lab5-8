package org.protocol;

import java.io.Serial;
import java.io.Serializable;

public final class CommandRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final CommandType commandType;
    private final String stringArgument;
    private final Integer integerArgument;
    private final MovieData movieData;

    private CommandRequest(
        CommandType commandType,
        String stringArgument,
        Integer integerArgument,
        MovieData movieData
    ) {
        this.commandType = commandType;
        this.stringArgument = stringArgument;
        this.integerArgument = integerArgument;
        this.movieData = movieData;
    }

    public static CommandRequest of(CommandType commandType) {
        return new CommandRequest(commandType, null, null, null);
    }

    public static CommandRequest withString(
        CommandType commandType,
        String stringArgument
    ) {
        return new CommandRequest(commandType, stringArgument, null, null);
    }

    public static CommandRequest withInteger(
        CommandType commandType,
        Integer integerArgument
    ) {
        return new CommandRequest(commandType, null, integerArgument, null);
    }

    public static CommandRequest withMovie(
        CommandType commandType,
        String stringArgument,
        MovieData movieData
    ) {
        return new CommandRequest(commandType, stringArgument, null, movieData);
    }

    public static CommandRequest withMovie(
        CommandType commandType,
        Integer integerArgument,
        MovieData movieData
    ) {
        return new CommandRequest(commandType, null, integerArgument, movieData);
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getStringArgument() {
        return stringArgument;
    }

    public Integer getIntegerArgument() {
        return integerArgument;
    }

    public MovieData getMovieData() {
        return movieData;
    }
}
