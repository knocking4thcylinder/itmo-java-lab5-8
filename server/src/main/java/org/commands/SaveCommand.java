package org.commands;

/**
 * Команда для сохранения коллекции в файл.
 */

public class SaveCommand extends ServerCommand {

    /**
     * Сообщает, что XML-сохранение больше не используется.
     *
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(ServerContext context) {
        return "save is obsolete: the server now uses PostgreSQL as storage";
    }
}
