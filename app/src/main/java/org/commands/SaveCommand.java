package org.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.dataclasses.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Команда для сохранения коллекции в файл.
 */

public class SaveCommand extends ServerCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveCommand.class);

    /**
     * Сохраняет коллекцию в файл.
     * @param context серверный контекст
     * @return результат выполнения
     */
    @Override
    public String exec(ServerContext context) {
        try (
            FileOutputStream outputStream = new FileOutputStream(
                new File(context.storagePath().toString()),
                false
            )
        ) {
            String serializedCollection = context.collectionManager()
                .getCollection()
                .entrySet()
                .stream()
                .map(SaveCommand::serializeEntry)
                .collect(Collectors.joining("\n"));
            String xmlDocument =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                serializedCollection +
                (serializedCollection.isEmpty() ? "" : "\n");
            outputStream.write(xmlDocument.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            return (
                "Successfully saved the collection to file \"" +
                context.storagePath() +
                "\""
            );
        } catch (FileNotFoundException e) {
            LOGGER.error(
                "Cannot write the file on path {}, check write permissions",
                context.storagePath(),
                e
            );
        } catch (UncheckedIOException e) {
            LOGGER.error(
                "Failed to serialize collection while saving to {}",
                context.storagePath(),
                e
            );
        } catch (IOException e) {
            LOGGER.error("I/O error while saving collection to {}", context.storagePath(), e);
        }
        return "Failed to save collection";
    }

    private static String serializeEntry(Map.Entry<String, Movie> entry) {
        try {
            return entry.getValue().toXML(entry.getKey());
        } catch (XMLStreamException e) {
            throw new UncheckedIOException(
                new IOException("Failed to serialize movie entry", e)
            );
        }
    }
}
