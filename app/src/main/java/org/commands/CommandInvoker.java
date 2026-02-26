package org.commands;

import java.lang.reflect.Method;
import java.util.HashMap;

public class CommandInvoker {

    HashMap<String, Class<?>> commandMap = new HashMap<>();

    public CommandInvoker() {
        commandMap.put("help", HelpCommand.class);
        commandMap.put("save", SaveCommand.class);
        commandMap.put("info", InfoCommand.class);
    }

    public void invoke(String name, String... args) {
        try {
            Method exec = commandMap
                .get(name)
                .getMethod("exec", String[].class);
            exec.invoke(null, (Object) args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
