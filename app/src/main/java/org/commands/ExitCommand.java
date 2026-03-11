package org.commands;

/**
 * Команда для завершения программы.
 */

public class ExitCommand implements Executable {

    /**
     * Завершает программу.
     * @param args аргументы команды
     * @return пустая строка
     */
    @Override
    public String exec(String... args) {
        System.exit(0);
        return "";
    }
}
