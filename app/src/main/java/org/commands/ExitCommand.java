package org.commands;

import java.io.Serializable;

/**
 * Команда для завершения программы.
 */

public class ExitCommand implements Executable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Завершает программу.
     * @return пустая строка
     */
    @Override
    public String exec() {
        System.exit(0);
        return "";
    }
}
