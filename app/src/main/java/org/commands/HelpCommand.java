package org.commands;

class HelpCommand implements ExecutableInterface {

    public static void exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"help\" does not accept any arguments"
            );
        }
        System.out.println(
            """
            help - shows help for all of the commands
            info - shows information about the current state of the Collection
            save - saves the current state of the collection to a file with the name given to the app on startup
            """
        );
    }
}
