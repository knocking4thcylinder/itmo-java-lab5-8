package org.dataclasses;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;

/**
 * Класс для хранения данных о фильме.
 */

public class Movie implements Comparable<Movie>, Serializable {

    private static final long serialVersionUID = 1L;

    private static int minNotUsedId = 100000;
    private int id;
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate;
    private int oscarsCount;
    private MovieGenre genre;
    private MpaaRating mpaaRating;
    private Person operator;

    /**
     * Конструктор по умолчанию.
     * Создает фильм с автогенерированными id и датой создания.
     */
    public Movie() {
        this.id = ++minNotUsedId;
        this.creationDate = LocalDateTime.now();
    }

    /**
     * Конструктор с параметрами.
     * Создает фильм с указанными параметрами, id и дата создания генерируются автоматически.
     * @param name название фильма (не может быть пустым или null)
     * @param coordinates координаты (не могут быть null)
     * @param oscarsCount количество оскаров (должно быть больше 0)
     * @param genre жанр фильма (не может быть null)
     * @param mpaaRating рейтинг MPAA (не может быть null)
     * @param operator оператор фильма (может быть null)
     * @throws IllegalArgumentException если name пустой или null
     * @throws IllegalArgumentException если coordinates равно null
     * @throws IllegalArgumentException если oscarsCount меньше или равно 0
     * @throws IllegalArgumentException если genre равно null
     * @throws IllegalArgumentException если mpaaRating равно null
     */
    public Movie(
        String name,
        Coordinates coordinates,
        int oscarsCount,
        MovieGenre genre,
        MpaaRating mpaaRating,
        Person operator
    ) {
        this.id = ++minNotUsedId;
        this.creationDate = LocalDateTime.now();
        if (name == null || name.isBlank()) {
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

    /**
     * Устанавливает минимальный неиспользованный id.
     * Используется при загрузке коллекции из файла для генерации уникальных id.
     * @param id минимальный неиспользованный id
     */
    public static void setMinNotUsedId(int id) {
        if (id > minNotUsedId) {
            minNotUsedId = id;
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

    /**
     * Сериализует фильм в XML формат.
     * @param key ключ, который будет ассоциирован с фильмом в XML
     * @return строковое представление фильма в XML формате
     * @throws XMLStreamException при ошибке сериализации в XML
     */
    public String toXML(String key) throws XMLStreamException {
        StringWriter xmlStream = new StringWriter();
        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(
            xmlStream
        );
        XMLEventFactory eventFactory = XMLEventFactory.newFactory();

        try {
            // eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
            // eventWriter.add(eventFactory.createCharacters("\n"));
            Attribute attribure = eventFactory.createAttribute("key", key);
            eventWriter.add(
                eventFactory.createStartElement(
                    "",
                    "",
                    "Movie",
                    List.of(attribure).iterator(),
                    null
                )
            );
            writeObjectToXML(this, eventWriter, eventFactory, 1);
            eventWriter.add(eventFactory.createCharacters("\n"));
            eventWriter.add(eventFactory.createEndElement("", "", "Movie"));
            // eventWriter.add(eventFactory.createCharacters("\n"));
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

    private static <T> T readObjectFromXML(
        XMLEventReader eventReader,
        Class<T> objectClass
    ) throws Exception {
        T instance = objectClass.getDeclaredConstructor().newInstance();
        int parsingDepth = 0;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.peek();

            if (event.isStartElement()) {
                parsingDepth++;
                StartElement startElement = eventReader
                    .nextEvent()
                    .asStartElement();
                String fieldName = startElement.getName().getLocalPart();
                String fieldSetterName =
                    "set" +
                    Character.toUpperCase(fieldName.charAt(0)) +
                    fieldName.substring(1);

                Field field;
                Method fieldSetter;
                try {
                    field = objectClass.getDeclaredField(fieldName);
                    fieldSetter = objectClass.getDeclaredMethod(
                        fieldSetterName,
                        field.getType()
                    );
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    break;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    break;
                }

                fieldSetter.setAccessible(true);

                Class<?> fieldType = field.getType();
                Object value;

                if (
                    eventReader.peek().isCharacters() &&
                    !eventReader.peek().asCharacters().isWhiteSpace()
                ) {
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
                fieldSetter.invoke(instance, value);
            } else if (event.isEndElement()) {
                eventReader.nextEvent();
                if (parsingDepth-- <= 0) {
                    break;
                }
            } else {
                eventReader.nextEvent();
            }
        }
        return instance;
        // throw new XMLStreamException(
        //     "Reached end of document unexpectedly while parsing " +
        //         objectClass.getSimpleName()
        // );
    }

    /**
     * Десериализует фильм из XML формата.
     * @param inputString строковое представление фильма в XML формате
     * @return пара (ключ, фильм), где ключ - атрибут key из XML, а фильм - десериализованный объект
     * @throws XMLStreamException при ошибке парсинга XML
     * @throws Exception при ошибке создания объекта из XML
     */
    public static Map.Entry<String, Movie> fromXML(String inputString)
        throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(
            new StringReader(inputString)
        );

        while (eventReader.hasNext()) {
            if (eventReader.peek().isStartElement()) {
                StartElement startElement = eventReader.peek().asStartElement();
                if (startElement.getName().getLocalPart().equals("Movie")) {
                    String key = startElement
                        .getAttributeByName(new QName("key"))
                        .getValue();
                    eventReader.nextEvent();
                    Movie movie = readObjectFromXML(eventReader, Movie.class);
                    Movie.minNotUsedId =
                        Math.max(movie.getId(), Movie.minNotUsedId) + 1;
                    return Map.entry(key, movie);
                }
            }
            eventReader.nextEvent();
        }
        throw new XMLStreamException("Could not find <Movie> tag in the XML.");
    }

    /**
     * Сравнивает фильмы по названию, количеству оскаров и id.
     * Сначала сравнивается название, затем количество оскаров, затем id.
     * @param o фильм для сравнения
     * @return отрицательное число, если этот фильм меньше, положительное если больше, 0 если равны
     */
    @Override
    public int compareTo(Movie o) {
        if (o == null) {
            return 1;
        }
        return Comparator.comparing(Movie::getName)
            .thenComparingInt(Movie::getOscarsCount)
            .thenComparingInt(Movie::getId)
            .compare(this, o);
    }

    /**
     * Возвращает дату создания фильма.
     * @return дата создания фильма
     */
    public java.time.LocalDateTime getCreationDate() {
        return this.creationDate;
    }

    /**
     * Возвращает уникальный идентификатор фильма.
     * @return id фильма
     */
    public int getId() {
        return this.id;
    }

    /**
     * Возвращает название фильма.
     * @return название фильма
     */
    public String getName() {
        return this.name;
    }

    /**
     * Возвращает координаты фильма.
     * @return координаты фильма
     */
    public Coordinates getCoordinates() {
        return this.coordinates;
    }

    /**
     * Возвращает количество оскаров у фильма.
     * @return количество оскаров
     */
    public int getOscarsCount() {
        return this.oscarsCount;
    }

    /**
     * Возвращает жанр фильма.
     * @return жанр фильма
     */
    public MovieGenre getGenre() {
        return this.genre;
    }

    /**
     * Возвращает рейтинг MPAA фильма.
     * @return рейтинг MPAA
     */
    public MpaaRating getMpaaRating() {
        return this.mpaaRating;
    }

    /**
     * Возвращает оператора фильма.
     * @return оператор фильма (может быть null)
     */
    public Person getOperator() {
        return this.operator;
    }

    /**
     * Устанавливает название фильма.
     * @param name новое название фильма (не может быть пустым или null)
     * @throws IllegalArgumentException если name пустой или null
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Movie.name cannot be empty or null"
            );
        }
        this.name = name;
    }

    /**
     * Устанавливает координаты фильма.
     * @param coordinates новые координаты фильма (не могут быть null)
     * @throws IllegalArgumentException если coordinates равно null
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = Objects.requireNonNull(
            coordinates,
            "Movie.coordinates cannot be null"
        );
    }

    /**
     * Устанавливает количество оскаров фильма.
     * @param oscarsCount новое количество оскаров (должно быть больше 0)
     * @throws IllegalArgumentException если oscarsCount меньше или равно 0
     */
    public void setOscarsCount(int oscarsCount) {
        if (oscarsCount <= 0) {
            throw new IllegalArgumentException(
                "Movie.oscarsCount must be greater than zero"
            );
        }
        this.oscarsCount = oscarsCount;
    }

    /**
     * Устанавливает жанр фильма.
     * @param genre новый жанр фильма (не может быть null)
     * @throws IllegalArgumentException если genre равно null
     */
    public void setGenre(MovieGenre genre) {
        this.genre = Objects.requireNonNull(
            genre,
            "Movie.genre cannot be null"
        );
    }

    /**
     * Устанавливает рейтинг MPAA фильма.
     * @param mpaaRating новый рейтинг MPAA (не может быть null)
     * @throws IllegalArgumentException если mpaaRating равно null
     */
    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = Objects.requireNonNull(
            mpaaRating,
            "Movie.mpaaRating cannot be null"
        );
    }

    private void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    private void setId(int id) {
        this.id = id;
    }

    /**
     * Устанавливает оператора фильма.
     * @param operator новый оператор фильма (может быть null)
     */
    public void setOperator(Person operator) {
        this.operator = operator;
    }

    /**
     * Возвращает строковое представление фильма.
     * @return строковое представление фильма со всеми полями
     */
    @Override
    public String toString() {
        return (
            "Movie{" +
            "id=" +
            id +
            ", name='" +
            unescapeXml(name) +
            '\'' +
            ", coordinates=" +
            coordinates +
            ", creationDate=" +
            creationDate +
            ", oscarsCount=" +
            oscarsCount +
            ", genre=" +
            genre +
            ", mpaaRating=" +
            mpaaRating +
            ", operator=" +
            operator +
            '}'
        );
    }

    private String unescapeXml(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder(
            input
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&lt;",   "<")
                .replace("&gt;",   ">")
                .replace("&amp;",  "&")
        );
        for (int i = 0; i < sb.length() - 1; i++) {
            if (sb.charAt(i) == '\\') {
                switch (sb.charAt(i + 1)) {
                    case 'n'  -> { sb.replace(i, i + 2, "\n"); }
                    case 't'  -> { sb.replace(i, i + 2, "\t"); }
                    case '\\' -> { sb.replace(i, i + 2, "\\"); }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Проверяет равенство фильмов.
     * Фильмы считаются равными, если у них одинаковые id, название, координаты,
     * дата создания, количество оскаров, жанр, рейтинг MPAA и оператор.
     * @param o объект для сравнения
     * @return true если фильмы равны, false в противном случае
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return (
            id == movie.id &&
            oscarsCount == movie.oscarsCount &&
            Objects.equals(name, movie.name) &&
            Objects.equals(coordinates, movie.coordinates) &&
            Objects.equals(creationDate, movie.creationDate) &&
            genre == movie.genre &&
            mpaaRating == movie.mpaaRating &&
            Objects.equals(operator, movie.operator)
        );
    }

    /**
     * Возвращает хэш-код фильма.
     * Хэш-код вычисляется на основе всех полей фильма.
     * @return хэш-код фильма
     */
    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            name,
            coordinates,
            creationDate,
            oscarsCount,
            genre,
            mpaaRating,
            operator
        );
    }
}
