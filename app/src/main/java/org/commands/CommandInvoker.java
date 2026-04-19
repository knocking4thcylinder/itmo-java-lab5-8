package org.commands;

/**
 * Класс для управления и выполнения команд.
 */
public class CommandInvoker {

    /**
     * Выполняет команду.
     *
     * @param command объект команды
     * @return результат выполнения команды
     * @throws Exception если во время выполнения произошла ошибка
     */
    public String invoke(Executable command) throws Exception {
        return command.exec();
    }
}
