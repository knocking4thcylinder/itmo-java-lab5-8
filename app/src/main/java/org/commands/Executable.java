package org.commands;

/**
 * Интерфейс для исполняемых команд приложения.
 */
interface Executable {
    /**
     * Выполняет команду с указанными аргументами.
     * @param args аргументы команды
     * @return результат выполнения команды
     * @throws Exception при ошибке выполнения
     */
    String exec(String... args) throws Exception;
}
