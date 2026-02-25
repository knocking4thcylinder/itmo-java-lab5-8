package org.dataclasses;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;

public class Movie implements Comparable<Movie> {

    private static HashMap<String, Class<?>> setterArgumentMap = new HashMap<
        String,
        Class<?>
    >();

    static {
        setterArgumentMap.put("setID", int.class);
        setterArgumentMap.put("setMinNotUsedID", int.class);
        setterArgumentMap.put("setName", String.class);
        setterArgumentMap.put("setCoordinates", Coordinates.class);
        setterArgumentMap.put("setOscarsCount", int.class);
        setterArgumentMap.put("setGenre", MovieGenre.class);
        setterArgumentMap.put("setMpaaRating", MpaaRating.class);
        setterArgumentMap.put("setOperator", Person.class);
        setterArgumentMap.put("setId", int.class);
        setterArgumentMap.put("setCreationDate", java.time.LocalDateTime.class);
    }

    private static int minNotUsedID = 0;
    private int id;
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate;
    private int oscarsCount;
    private MovieGenre genre;
    private MpaaRating mpaaRating;
    private Person operator;

    public Movie() {
        this.id = minNotUsedID++;
        this.creationDate = LocalDateTime.now();
    }

    public Movie(
        String name,
        Coordinates coordinates,
        int oscarsCount,
        MovieGenre genre,
        MpaaRating mpaaRating,
        Person operator
    ) {
        this.id = minNotUsedID++;
        this.creationDate = LocalDateTime.now();
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException(
                "Movie.name cannot be empty or null"
            );
        }
        this.name = name;
        this.coordinates = Objects.requireNonNull(
            coordinates,
            "Movie.coordinates cannot be null"
        );
        if (oscarsCount <= 0) {
            throw new IllegalArgumentException(
                "Movie.oscarsCount must be greater than zero"
            );
        }
        this.oscarsCount = oscarsCount;
        this.genre = Objects.requireNonNull(
            genre,
            "Movie.genre cannot be null"
        );
        this.mpaaRating = Objects.requireNonNull(
            mpaaRating,
            "Movie.mpaaRating cannot be null"
        );
        this.operator = operator;
    }

    public static void setMinNotUsedID(int id) {
        if (id > minNotUsedID) {
            minNotUsedID = id;
        }
    }

    private void writeObjectToXML(
        Object object,
        XMLEventWriter eventWriter,
        XMLEventFactory eventFactory,
        int indent
    ) throws XMLStreamException, IllegalAccessException {
        Field[] fields = object.getClass().getDeclaredFields();
        String indentStr = "\t".repeat(indent);
        String endAndIndentStr = "\n" + indentStr;
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null) {
                continue;
            }
            String fieldName = field.getName();

            eventWriter.add(eventFactory.createCharacters(endAndIndentStr));
            eventWriter.add(eventFactory.createStartElement("", "", fieldName));

            Class<?> fieldType = field.getType();
            if (
                fieldType.isPrimitive() ||
                value instanceof String ||
                value instanceof Number ||
                value instanceof java.time.LocalDateTime ||
                fieldType.isEnum()
            ) {
                eventWriter.add(
                    eventFactory.createCharacters(value.toString())
                );
            } else {
                writeObjectToXML(value, eventWriter, eventFactory, indent + 1);
                eventWriter.add(eventFactory.createCharacters(endAndIndentStr));
            }
            eventWriter.add(eventFactory.createEndElement("", "", fieldName));
        }
    }

    //TODO
    public String toXML() throws XMLStreamException {
        StringWriter xmlStream = new StringWriter();
        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(
            xmlStream
        );
        XMLEventFactory eventFactory = XMLEventFactory.newFactory();

        try {
            // eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
            // eventWriter.add(eventFactory.createCharacters("\n"));
            eventWriter.add(eventFactory.createStartElement("", "", "Movie"));
            writeObjectToXML(this, eventWriter, eventFactory, 1);
            eventWriter.add(eventFactory.createCharacters("\n"));
            eventWriter.add(eventFactory.createEndElement("", "", "Movie"));
            eventWriter.add(eventFactory.createCharacters("\n"));
            // eventWriter.add(eventFactory.createEndDocument());
        } catch (IllegalAccessException e) {
            throw new XMLStreamException(
                "Error accessing field during XML serialization.",
                e
            );
        } finally {
            eventWriter.close();
        }
        return xmlStream.toString();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> T readObjectFromXML(
        XMLEventReader eventReader,
        Class<T> objectClass
    ) throws Exception {
        T instance = objectClass.getDeclaredConstructor().newInstance();

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.peek();

            if (event.isStartElement()) {
                StartElement startElement = eventReader
                    .nextEvent()
                    .asStartElement();
                String fieldName = startElement.getName().getLocalPart();

                Field field;
                try {
                    field = objectClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    break;
                }

                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object value;

                if (eventReader.peek().isCharacters()) {
                    String stringValue = eventReader
                        .nextEvent()
                        .asCharacters()
                        .getData()
                        .trim();

                    if (fieldType.isEnum()) {
                        value = Enum.valueOf(
                            (Class<Enum>) fieldType,
                            stringValue
                        );
                    } else {
                        value = switch (fieldType.getName()) {
                            case "java.lang.String" -> stringValue;
                            case "int", "java.lang.Integer" -> Integer.parseInt(
                                stringValue
                            );
                            case "long", "java.lang.Long" -> Long.parseLong(
                                stringValue
                            );
                            case
                                "double",
                                "java.lang.Double" -> Double.parseDouble(
                                stringValue
                            );
                            case "java.time.LocalDateTime" -> LocalDateTime.parse(
                                stringValue
                            );
                            default -> throw new IllegalArgumentException(
                                "Unsupported type for conversion: " +
                                    fieldType.getSimpleName()
                            );
                        };
                    }
                } else {
                    value = readObjectFromXML(eventReader, fieldType);
                }
                field.set(instance, value);
            } else if (event.isEndElement()) {
                eventReader.nextEvent();
                return instance;
            } else {
                eventReader.nextEvent();
            }
        }
        throw new XMLStreamException(
            "Reached end of document unexpectedly while parsing " +
                objectClass.getSimpleName()
        );
    }

    //TODO
    public static Movie fromXML(String inputString) throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(
            new StringReader(inputString)
        );

        while (eventReader.hasNext()) {
            if (eventReader.peek().isStartElement()) {
                StartElement startElement = eventReader.peek().asStartElement();
                if (startElement.getName().getLocalPart().equals("Movie")) {
                    eventReader.nextEvent();
                    return readObjectFromXML(eventReader, Movie.class);
                }
            }
            eventReader.nextEvent();
        }
        throw new XMLStreamException("Could not find <Movie> tag in the XML.");
    }

    @Override
    public int compareTo(Movie o) {
        if (o == null) {
            return 1;
        }
        return Comparator.comparing(Movie::getName)
            .thenComparingInt(Movie::getOscarsCount)
            .thenComparingInt(Movie::getID)
            .compare(this, o);
    }

    public java.time.LocalDateTime getCreationDate() {
        return this.creationDate;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Coordinates getCoordinates() {
        return this.coordinates;
    }

    public int getOscarsCount() {
        return this.oscarsCount;
    }

    public MovieGenre getGenre() {
        return this.genre;
    }

    public MpaaRating getMpaaRating() {
        return this.mpaaRating;
    }

    public Person getOperator() {
        return this.operator;
    }

    public void setName(String name) {
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException(
                "Movie.name cannot be empty or null"
            );
        }
        this.name = name;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = Objects.requireNonNull(
            coordinates,
            "Movie.coordinates cannot be null"
        );
    }

    public void setOscarsCount(int oscarsCount) {
        if (oscarsCount <= 0) {
            throw new IllegalArgumentException(
                "Movie.oscarsCount must be greater than zero"
            );
        }
        this.oscarsCount = oscarsCount;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = Objects.requireNonNull(
            genre,
            "Movie.genre cannot be null"
        );
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = Objects.requireNonNull(
            mpaaRating,
            "Movie.mpaaRating cannot be null"
        );
    }

    public void setOperator(Person operator) {
        this.operator = operator;
    }
}
