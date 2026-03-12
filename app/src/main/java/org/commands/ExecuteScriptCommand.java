package org.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.App;

/**
 * Команда для выполнения скрипта из файла.
 */

public class ExecuteScriptCommand implements Executable {

    private static java.util.Set<String> executingScripts =
        java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    /**
     * Выполняет скрипт из файла.
     * @param args аргументы команды, где args[0] - имя файла
     * @return результаты выполнения команд скрипта
     * @throws FileNotFoundException при отсутствии файла
     * @throws AccessDeniedException при отсутствии прав доступа
     */
    @Override
    public String exec(String... args)
        throws FileNotFoundException, AccessDeniedException {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"execute_script\" accepts exactly one argument"
            );
        }
        String fileName = args[0];
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
        StringBuilder sb = new StringBuilder();

        try {
            inputParser.setInputSource(
                new ScannerInputSource(Files.newInputStream(Paths.get(fileName)))
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
                    String result = commandInvoker.invoke(
                        command[0],
                        Arrays.copyOfRange(command, 1, command.length)
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
