package org.commands;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Клиентская команда выполнения скрипта.
 */
public class ExecuteScriptCommand extends ClientCommand {

    private final String fileName;

    /**
     * Создает команду выполнения скрипта.
     *
     * @param fileName имя файла со скриптом
     */
    public ExecuteScriptCommand(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                "Script file name cannot be null or blank"
            );
        }
        this.fileName = fileName;
    }

    /**
     * Возвращает имя файла скрипта.
     *
     * @return имя файла
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Выполняет команды из скрипта последовательно.
     *
     * @param context клиентский контекст
     * @return пустая строка, так как результаты печатаются по мере выполнения
     */
    @Override
    public String exec(ClientContext context) throws Exception {
        if (context.isExecutingScript(fileName)) {
            return "Cannot execute script recursively: " + fileName;
        }

        var scriptPath = Paths.get(fileName);
        if (!Files.exists(scriptPath)) {
            throw new FileNotFoundException("no file found on path " + fileName);
        }
        if (!Files.isReadable(scriptPath)) {
            throw new AccessDeniedException(
                "cant read the file on path " +
                fileName +
                ", check read permissions"
            );
        }

        ClientContext newClientContext = context.copyWithInputSource(
            new ScannerInputSource(Files.newInputStream(scriptPath))
        );
        InputParser inputParser = newClientContext.inputParser();
        context.beginScript(fileName);
        try {
            for (String[] nestedCommand : inputParser) {
                if (nestedCommand.length == 0 || nestedCommand[0].isEmpty()) {
                    continue;
                }
                try {
                    String result = newClientContext.commandInvoker().invoke(
                        newClientContext.commandFactory().create(
                            nestedCommand[0],
                            Arrays.copyOfRange(
                                nestedCommand,
                                1,
                                nestedCommand.length
                            )
                        ),
                        newClientContext
                    );
                    if (result != null && !result.isEmpty()) {
                        System.out.println(result);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } finally {
            context.endScript(fileName);
        }
        return "";
    }
}
