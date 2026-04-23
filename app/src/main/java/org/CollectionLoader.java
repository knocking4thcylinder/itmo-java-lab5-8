package org;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.dataclasses.Movie;

/**
 * Загружает коллекцию фильмов из XML-файла.
 */
public final class CollectionLoader {

    private CollectionLoader() {}

    /**
     * Загружает коллекцию из XML-файла.
     *
     * @param inputPath путь к файлу хранения
     * @return загруженная коллекция
     * @throws FileNotFoundException если файл не найден
     * @throws AccessDeniedException если нет прав доступа к файлу
     */
    public static TreeMap<String, Movie> load(Path inputPath)
        throws FileNotFoundException, AccessDeniedException {
        if (!Files.isReadable(inputPath)) {
            throw new AccessDeniedException(
                "cant read the file on path " +
                inputPath +
                ", check read permissions"
            );
        }
        if (!Files.isWritable(inputPath)) {
            throw new AccessDeniedException(
                "cant write the file on path " +
                inputPath +
                ", check write permissions"
            );
        }

        Pattern pattern = Pattern.compile(
            "<Movie.*?>.*?</Movie>",
            Pattern.DOTALL
        );
        TreeMap<String, Movie> loadedCollection = new TreeMap<>();
        try (Scanner scanner = new Scanner(Files.newInputStream(inputPath), "UTF-8")) {
            while (scanner.findWithinHorizon(pattern, 0) != null) {
                Map.Entry<String, Movie> movie = Movie.fromXML(
                    scanner.match().group(0)
                );
                loadedCollection.put(movie.getKey(), movie.getValue());
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                "no file found on path " + inputPath
            );
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException(
                "cant read the file on path " +
                inputPath +
                ", check read permissions"
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to load collection from " + inputPath,
                e
            );
        }
        return loadedCollection;
    }
}
