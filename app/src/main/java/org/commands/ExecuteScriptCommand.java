package org.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Клиентская команда выполнения скрипта.
 */
public class ExecuteScriptCommand extends ClientCommand {

    private static final Set<String> executingScripts =
        Collections.synchronizedSet(new HashSet<>());

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
     * Выполняет скрипт из локального файла клиента.
     *
     * @param context клиентский контекст
     * @return суммарный результат выполнения команд скрипта
     * @throws Exception если возникает ошибка чтения или выполнения
     */
    @Override
    public String exec(ClientContext context) throws Exception {
        if (executingScripts.contains(fileName)) {
            return "Cannot execute script recursively: " + fileName;
        }

        Path scriptPath = Paths.get(fileName);
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

        InputParser inputParser = context.inputParser();
        InputSource previousInputSource = inputParser.getInputSource();
        StringBuilder sb = new StringBuilder();
        executingScripts.add(fileName);
        try {
            inputParser.setInputSource(
                new ScannerInputSource(Files.newInputStream(scriptPath))
            );
            for (String[] nestedCommand : inputParser) {
                if (nestedCommand.length == 0 || nestedCommand[0].isEmpty()) {
                    continue;
                }
                try {
                    String result = context.dispatch(nestedCommand);
                    if (result != null && !result.isEmpty()) {
                        if (!sb.isEmpty()) {
                            sb.append("\n");
                        }
                        sb.append(result);
                    }
                } catch (Exception e) {
                    if (!sb.isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append(e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IOException("Error reading script file", e);
        } finally {
            executingScripts.remove(fileName);
            inputParser.setInputSource(previousInputSource);
        }
        return sb.toString();
    }
}
