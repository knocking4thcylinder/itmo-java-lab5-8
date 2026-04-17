package org.server.collection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import org.dataclasses.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XmlCollectionStorage {

    private static final Logger logger = LoggerFactory.getLogger(
        XmlCollectionStorage.class
    );

    private static final Pattern MOVIE_PATTERN = Pattern.compile(
        "<Movie.*?>.*?</Movie>",
        Pattern.DOTALL
    );

    private final Path path;

    public XmlCollectionStorage(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public TreeMap<String, Movie> load() throws Exception {
        TreeMap<String, Movie> loadedCollection = new TreeMap<>();
        if (Files.notExists(path)) {
            logger.info("Collection file {} does not exist yet, starting empty", path);
            return loadedCollection;
        }

        try (Scanner scanner = new Scanner(Files.newInputStream(path), "UTF-8")) {
            while (scanner.findWithinHorizon(MOVIE_PATTERN, 0) != null) {
                Map.Entry<String, Movie> movie = Movie.fromXML(
                    scanner.match().group(0)
                );
                loadedCollection.put(movie.getKey(), movie.getValue());
            }
            logger.info("Loaded {} entries from {}", loadedCollection.size(), path);
            return loadedCollection;
        } catch (FileNotFoundException exception) {
            throw new FileNotFoundException(
                "No file found on path " + path
            );
        }
    }

    public void save(TreeMap<String, Movie> collection) throws Exception {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (
            FileOutputStream outputStream = new FileOutputStream(
                path.toFile(),
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
            for (Map.Entry<String, Movie> movie : collection.entrySet()) {
                outputStream.write(
                    movie.getValue().toXML(movie.getKey()).getBytes()
                );
                eventWriter.add(eventFactory.createCharacters("\n"));
            }
            eventWriter.add(eventFactory.createEndDocument());
            eventWriter.close();
            logger.info("Saved {} entries to {}", collection.size(), path);
        } catch (IOException exception) {
            throw new IOException(
                "Failed to save collection to " + path,
                exception
            );
        }
    }
}
