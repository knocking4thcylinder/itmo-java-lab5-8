package org.commands;

/**
 * Класс для управления и выполнения команд.
 */
public class CommandInvoker {

    /**
     * Выполняет команду.
     *
     * @param command объект команды
     * @param context контекст выполнения команды
     * @return результат выполнения команды
     * @throws Exception если во время выполнения произошла ошибка
     */
    public <C> String invoke(
        ExecutableWithContext<C> command,
        C context
    ) throws Exception {
        return command.exec(context);
    }
}
