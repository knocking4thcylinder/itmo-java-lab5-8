package org.commands;

/**
 * Клиентская команда завершения приложения.
 */
public class ExitCommand extends ClientCommand {

    /**
     * Завершает клиентское приложение.
     *
     * @param context клиентский контекст
     * @return недостижимое значение
     */
    @Override
    public String exec(ClientContext context) {
        System.exit(0);
        return "";
    }
}
