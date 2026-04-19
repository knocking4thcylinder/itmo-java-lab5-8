package org.commands;

import org.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Команда для выполнения скрипта из файла.
 */

public class ExecuteScriptCommand implements Executable, Serializable {

    private static final long serialVersionUID = 1L;

    private static java.util.Set<String> executingScripts =
        java.util.Collections.synchronizedSet(new java.util.HashSet<>());
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
     * Выполняет скрипт из файла.
     * @return результаты выполнения команд скрипта
     * @throws FileNotFoundException при отсутствии файла
     * @throws AccessDeniedException при отсутствии прав доступа
     */
    @Override
    public String exec()
        throws FileNotFoundException, AccessDeniedException {
        if (executingScripts.contains(fileName)) {
            return "Cannot execute script recursively: " + fileName;
        }

        File inputFile = new File(fileName);
        if (!inputFile.exists()) {
            throw new FileNotFoundException(
                "no file found on path " + fileName
            );
        } else if (!inputFile.canRead()) {
            throw new AccessDeniedException(
                "cant read the file on path " +
                    fileName +
                    ", check read permissions"
            );
        } else if (!inputFile.canWrite()) {
            throw new AccessDeniedException(
                "cant write the file on path " +
                    fileName +
                    ", check write permissions"
            );
        }

        CommandInvoker commandInvoker = new CommandInvoker();
        InputParser inputParser = App.getInputParser();
        CommandFactory commandFactory = new CommandFactory(inputParser);
        StringBuilder sb = new StringBuilder();

        try {
            inputParser.setInputSource(
                new ScannerInputSource(
                    Files.newInputStream(Paths.get(fileName))
                )
            );
        } catch (FileNotFoundException e) {
            inputParser.setInputSource(new ScannerInputSource(System.in));
            throw new FileNotFoundException(
                "no file found on path " + fileName
            );
        } catch (AccessDeniedException e) {
            inputParser.setInputSource(new ScannerInputSource(System.in));
            throw new AccessDeniedException(
                "cant read the file on path " +
                    fileName +
                    ", chech read permissions"
            );
        } catch (IOException e) {
            inputParser.setInputSource(new ScannerInputSource(System.in));
            e.printStackTrace();
            return "Error reading script file";
        }

        executingScripts.add(fileName);
        try {
            for (var command : inputParser) {
                try {
                    String commandName = command[0];

                    if (commandName.equalsIgnoreCase("execute_script")) {
                        throw new IllegalArgumentException(
                            "Recursive execution of 'execute_script' is not allowed"
                        );
                    }

                    String result = commandInvoker.invoke(
                        commandFactory.create(
                            commandName,
                            Arrays.copyOfRange(command, 1, command.length)
                        )
                    );
                    if (result != null && !result.isEmpty()) {
                        sb.append(result).append("\n");
                    }
                } catch (Exception e) {
                    sb.append(e.getMessage()).append("\n");
                }
            }
        } finally {
            executingScripts.remove(fileName);
            inputParser.setInputSource(new ScannerInputSource(System.in));
        }
        return sb.toString();
    }
}
