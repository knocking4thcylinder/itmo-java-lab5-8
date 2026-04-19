package org.commands;

/**
 * Интерфейс для исполняемых команд приложения.
 */
public interface Executable {
    /**
     * Выполняет команду.
     * @return результат выполнения команды
     * @throws Exception при ошибке выполнения
     */
    String exec() throws Exception;
}
