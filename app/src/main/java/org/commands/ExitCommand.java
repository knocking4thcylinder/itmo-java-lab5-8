package org.commands;

public class ExitCommand implements Executable {

    @Override
    public String exec(String... args) {
        System.exit(0);
        return "";
    }
}
