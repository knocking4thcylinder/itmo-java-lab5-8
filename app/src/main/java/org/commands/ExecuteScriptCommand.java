package org.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ExecuteScriptCommand implements Executable {

    public static void exec(String... args)
        throws FileNotFoundException, AccessDeniedException {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"execute_script\" accepts exactly one argument"
            );
        }
        String fileName = args[0];
        File inputFile = new File(fileName);
        if (!inputFile.exists()) {
            throw new FileNotFoundException(
                "no file found on path " + fileName
            );
        } else if (!inputFile.canRead()) {
            throw new AccessDeniedException(
                "cant read the file on path " +
                    fileName +
                    ", chech read permissions"
            );
        } else if (!inputFile.canWrite()) {
            throw new AccessDeniedException(
                "cant write the file on path " +
                    fileName +
                    ", chech write permissions"
            );
        }

        CommandInvoker commandInvoker = new CommandInvoker();

        try (
            InputParser fileParser = new InputParser(
                Files.newInputStream(Paths.get(fileName))
            )
        ) {
            for (var command : fileParser) {
                commandInvoker.invoke(
                    command[0],
                    Arrays.copyOfRange(command, 1, command.length)
                );
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                "no file found on path " + fileName
            );
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException(
                "cant read the file on path " +
                    fileName +
                    ", chech read permissions"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
