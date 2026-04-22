package org.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * Команда для сохранения коллекции в файл.
 */

public class SaveCommand extends ServerCommand {

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
            XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(
                outputStream
            );
            XMLEventFactory eventFactory = XMLEventFactory.newFactory();
            eventWriter.add(eventFactory.createStartDocument());
            eventWriter.add(eventFactory.createCharacters("\n"));
            for (var movie : context.collectionManager()
                .getCollection()
                .entrySet()) {
                outputStream.write(
                    movie.getValue().toXML(movie.getKey()).getBytes()
                );
                eventWriter.add(eventFactory.createCharacters("\n"));
            }
            eventWriter.add(eventFactory.createEndDocument());
            return (
                "Successfully saved the collection to file \"" +
                context.storagePath() +
                "\""
            );
        } catch (FileNotFoundException e) {
            System.out.println(
                "cant write the file on path " +
                    context.storagePath() +
                    ", check write permissions"
            );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return "Failed to save collection";
    }
}
