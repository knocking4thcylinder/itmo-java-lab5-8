package org.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.App;
import org.CollectionManager;

/**
 * Команда для сохранения коллекции в файл.
 */

public class SaveCommand implements Executable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Сохраняет коллекцию в файл.
     * @return результат выполнения
     */
    @Override
    public String exec() {
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
            System.out.println(
                "cant write the file on path " +
                    App.getStorageFile() +
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
