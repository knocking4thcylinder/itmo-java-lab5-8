package org.commands;

public class Command {

    private String name;
    private String[] args;

    public Command(String name, String... args) {
        this.name = name;
        this.args = args;
    }

    public String[] getArgs() {
        return this.args;
    }

    public String getName() {
        return this.name;
    }

    public void setArgs(String... args) {
        this.args = args;
    }
}
