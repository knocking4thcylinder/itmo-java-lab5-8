package org.commands;

/**
 * Интерфейс для команд, выполняемых в контексте.
 *
 * @param <C> тип контекста выполнения
 */
public interface ExecutableWithContext<C> {
    /**
     * Выполняет команду в заданном контексте.
     *
     * @param context контекст выполнения
     * @return результат выполнения
     * @throws Exception при ошибке выполнения
     */
    String exec(C context) throws Exception;
}
