package org.commands;

import java.util.Map;
import java.util.stream.Collectors;
import org.dataclasses.Movie;
import org.dataclasses.Person;
import org.dataclasses.Location;

/**
 * Returns a table-friendly collection snapshot for the Swing client.
 */
public class UiSnapshotCommand extends SharedCommand {

    private static final long serialVersionUID = 1L;
    @Override
    public String exec(SharedCommandContext context) {
        return context.visibleCollection()
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> row(entry.getKey(), entry.getValue(), context))
            .collect(Collectors.joining("\n"));
    }

    private String row(String key, Movie movie, SharedCommandContext context) {
        Person operator = movie.getOperator();
        Location location = operator == null ? null : operator.getLocation();
        String owner = context.ownerOf(key);
        return String.join(
            "\t",
            escape(key),
            Integer.toString(movie.getId()),
            escape(movie.getName()),
            Integer.toString(movie.getCoordinates().getX()),
            Integer.toString(movie.getCoordinates().getY()),
            Integer.toString(movie.getOscarsCount()),
            escape(String.valueOf(movie.getGenre())),
            escape(String.valueOf(movie.getMpaaRating())),
            escape(owner == null ? "" : owner),
            Boolean.toString(owner != null && owner.equals(context.ownerLogin())),
            escape(movie.getCreationDate() == null ? "" : movie.getCreationDate().toString()),
            escape(operator == null ? "" : operator.getName()),
            escape(operator == null ? "" : Double.toString(operator.getWeight())),
            escape(operator == null ? "" : String.valueOf(operator.getNationality())),
            escape(operator == null ? "" : nullToEmpty(operator.getPassportID())),
            escape(location == null ? "" : Long.toString(location.getX())),
            escape(location == null ? "" : Double.toString(location.getY())),
            escape(location == null ? "" : nullToEmpty(location.getName()))
        );
    }

    private String escape(String value) {
        return value == null
            ? ""
            : value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
