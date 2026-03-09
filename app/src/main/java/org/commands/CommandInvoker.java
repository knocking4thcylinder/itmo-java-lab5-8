package org.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;

public class CommandInvoker {

    HashMap<String, Class<?>> commandMap = new HashMap<>();

    public CommandInvoker() {
        commandMap.put("help", HelpCommand.class);
        commandMap.put("save", SaveCommand.class);
        commandMap.put("info", InfoCommand.class);
        commandMap.put("insert", InsertCommand.class);
        commandMap.put("update", UpdateCommand.class);
        commandMap.put("exit", ExitCommand.class);
        commandMap.put("clear", ClearComand.class);
        commandMap.put("remove_key", RemoveKeyCommand.class);
        commandMap.put("replace_if_lower", ReplaceIfLowerCommand.class);
        commandMap.put("remove_lower_key", RemoveLowerKeyCommand.class);
        commandMap.put("remove_greater_key", RemoveGreaterKeyCommand.class);
        commandMap.put("filter_by_genre", FilterByGenreCommand.class);
        commandMap.put("filter_contains_name", FilterContainsNameCommand.class);
        commandMap.put("execute_script", ExecuteScriptCommand.class);
        commandMap.put(
            "filter_less_than_mpaa_rating",
            FilterLessThanMpaaRatingCommand.class
        );
    }

    public void invoke(String name, String... args)
        throws NoSuchObjectException {
        Class<?> command = commandMap.get(name);
        if (command == null) {
            throw new NoSuchObjectException(
                "No command with name \"" + name + "\" exists"
            );
        }
        try {
            Method exec = command.getMethod("exec", String[].class);
            exec.invoke(null, (Object) args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println(e.getCause().getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
