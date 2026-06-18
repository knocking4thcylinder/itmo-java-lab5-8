package org.gui;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

record MovieRow(
    String key,
    int id,
    String name,
    int x,
    int y,
    int oscars,
    String genre,
    String rating,
    String owner,
    boolean editable,
    String created,
    String director,
    String weight,
    String country,
    String passportId,
    String locationX,
    String locationY,
    String locationName
) {
    static List<MovieRow> parse(String response) {
        if (response == null || response.isBlank()) {
            return List.of();
        }
        List<MovieRow> rows = new ArrayList<>();
        for (String line : response.split("\\R")) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 18) {
                continue;
            }
            rows.add(new MovieRow(
                unescape(parts[0]),
                Integer.parseInt(parts[1]),
                unescape(parts[2]),
                Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]),
                Integer.parseInt(parts[5]),
                unescape(parts[6]),
                unescape(parts[7]),
                unescape(parts[8]),
                Boolean.parseBoolean(parts[9]),
                unescape(parts[10]),
                unescape(parts[11]),
                unescape(parts[12]),
                unescape(parts[13]),
                unescape(parts[14]),
                unescape(parts[15]),
                unescape(parts[16]),
                unescape(parts[17])
            ));
        }
        return rows;
    }

    private static String unescape(String value) {
        return value.replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
    }

    String value(String column) {
        return switch (column) {
            case "key" -> key;
            case "id" -> Integer.toString(id);
            case "name" -> name;
            case "x" -> Integer.toString(x);
            case "y" -> Integer.toString(y);
            case "genre" -> genre;
            case "oscars" -> Integer.toString(oscars);
            case "rating" -> rating;
            case "director" -> director;
            case "weight" -> weight;
            case "country" -> country;
            case "passportId" -> passportId;
            case "locationX" -> locationX;
            case "locationY" -> locationY;
            case "locationName" -> locationName;
            case "owner" -> owner;
            case "created" -> created;
            default -> "";
        };
    }

    String displayValue(String column, Locale locale) {
        return switch (column) {
            case "weight", "locationY" -> formatDouble(value(column), locale);
            case "created" -> formatDate(value(column), locale);
            default -> value(column);
        };
    }

    private static String formatDouble(String value, Locale locale) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            NumberFormat format = NumberFormat.getNumberInstance(locale);
            format.setMaximumFractionDigits(3);
            return format.format(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static String formatDate(String value, Locale locale) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value);
            return DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale)
                .format(dateTime);
        } catch (DateTimeParseException e) {
            return value;
        }
    }

    static Comparator<MovieRow> comparator(String column) {
        return switch (column) {
            case "id" -> Comparator.comparingInt(MovieRow::id);
            case "x" -> Comparator.comparingInt(MovieRow::x);
            case "y" -> Comparator.comparingInt(MovieRow::y);
            case "oscars" -> Comparator.comparingInt(MovieRow::oscars);
            case "weight" -> Comparator.comparingDouble(row -> parseDouble(row.weight()));
            case "locationX" -> Comparator.comparingLong(row -> parseLong(row.locationX()));
            case "locationY" -> Comparator.comparingDouble(row -> parseDouble(row.locationY()));
            default -> Comparator.comparing(row -> row.value(column), String.CASE_INSENSITIVE_ORDER);
        };
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }
}
