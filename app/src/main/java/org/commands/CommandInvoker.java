package org.commands;

import java.rmi.NoSuchObjectException;
import java.util.HashMap;

public class CommandInvoker {

    HashMap<String, Executable> commandMap = new HashMap<>();

    public CommandInvoker() {
        commandMap.put("help", new HelpCommand());
        commandMap.put("save", new SaveCommand());
        commandMap.put("info", new InfoCommand());
        commandMap.put("show", new ShowCommand());
        commandMap.put("insert", new InsertCommand());
        commandMap.put("update", new UpdateCommand());
        commandMap.put("exit", new ExitCommand());
        commandMap.put("clear", new ClearComand());
        commandMap.put("remove_key", new RemoveKeyCommand());
        commandMap.put("replace_if_lower", new ReplaceIfLowerCommand());
        commandMap.put("remove_lower_key", new RemoveLowerKeyCommand());
        commandMap.put("remove_greater_key", new RemoveGreaterKeyCommand());
        commandMap.put("filter_by_genre", new FilterByGenreCommand());
        commandMap.put("filter_contains_name", new FilterContainsNameCommand());
        commandMap.put("execute_script", new ExecuteScriptCommand());
        commandMap.put(
            "filter_less_than_mpaa_rating",
            new FilterLessThanMpaaRatingCommand()
        );
    }

    public String invoke(String name, String... args) throws Exception {
        Executable command = commandMap.get(name);
        if (command == null) {
            throw new NoSuchObjectException(
                "No command with name \"" + name + "\" exists"
            );
        }
        return command.exec(args);
    }
}
