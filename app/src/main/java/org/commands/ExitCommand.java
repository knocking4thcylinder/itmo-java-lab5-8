package org.commands;

public class ExitCommand implements ExecutableInterface {

    public static void exec(String... args) {
        System.exit(0);
    }
}
