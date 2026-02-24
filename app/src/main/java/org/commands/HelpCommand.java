package org.commands;

class HelpCommand extends Command implements ExecutableInterface {

    public HelpCommand() {
        super("help");
    }

    @Override
    public void exec() {
        if (this.getArgs().length != 0) {
            throw new IllegalArgumentException(
                "command \"help\" does not accept any arguments"
            );
        }
        System.out.println(
            """
            help - shows help for all of the commands
            info - shows information about the current state of the Collection
            """
        );
    }
}
