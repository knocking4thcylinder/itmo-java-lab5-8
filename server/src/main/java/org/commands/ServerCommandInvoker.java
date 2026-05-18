package org.commands;

/**
 * Выполняет серверные команды в серверном контексте.
 */
public class ServerCommandInvoker {

    /**
     * Выполняет серверную команду.
     *
     * @param command серверная команда
     * @param context серверный контекст
     * @return результат выполнения
     * @throws Exception если во время выполнения произошла ошибка
     */
    public String invoke(ServerCommand command, ServerContext context)
        throws Exception {
        return command.exec(context);
    }

    /**
     * Выполняет общую команду в серверном контексте.
     *
     * @param command общая команда
     * @param context серверный контекст
     * @return результат выполнения
     * @throws Exception если во время выполнения произошла ошибка
     */
    public String invoke(SharedCommand command, ServerContext context)
        throws Exception {
        return command.exec(context);
    }
}
