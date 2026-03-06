package org.commands;

public class ExitCommand implements Executable {

    public static void exec(String... args) {
        System.exit(0);
    }
}
