package org.commands;

/**
 * Клиентская команда завершения приложения.
 */
public class ExitCommand extends ClientCommand {

    /**
     * Завершает клиентское приложение.
     *
     * @return недостижимое значение
     */
    @Override
    public String exec() {
        System.exit(0);
        return "";
    }
}
