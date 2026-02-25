package org.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.App;
import org.dataclasses.Movie;

public class SaveCommand implements ExecutableInterface {

    public static void exec(String... args) {
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
            for (Movie movie : App.getCollection().values()) {
                outputStream.write(movie.toXML().getBytes());
                eventWriter.add(eventFactory.createCharacters("\n"));
            }
            eventWriter.add(eventFactory.createEndDocument());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
