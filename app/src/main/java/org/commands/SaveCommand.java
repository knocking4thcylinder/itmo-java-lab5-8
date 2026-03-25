package org.commands;

import org.App;
import org.CollectionManager;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Команда для сохранения коллекции в файл.
 */

public class SaveCommand implements Executable {

    /**
     * Сохраняет коллекцию в файл.
     * @param args аргументы команды
     * @return результат выполнения
     */
    @Override
    public String exec(String... args) {
        if (args.length != 0) {
            throw new IllegalArgumentException(
                "command \"save\" does not accept any arguments"
            );
        }
        try (
            FileOutputStream outputStream = new FileOutputStream(
                new File(App.getStorageFile()),
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
            for (var movie : CollectionManager.getInstance()
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
                App.getStorageFile() +
                "\""
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return "Failed to save collection";
    }
}
