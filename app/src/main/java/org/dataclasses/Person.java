package org.dataclasses;

import org.dataclasses.enums.Country;

import java.util.Objects;

/**
 * Класс для хранения данных об операторе фильма.
 * Содержит информацию об имени, весе, паспортных данных, национальности и местоположении человека.
 */
public class Person {

    private String name;
    private double weight;
    private String passportID;
    private Country nationality;
    private Location location;

    /**
     * Конструктор по умолчанию.
     * Создает объект Person с неопределенными значениями полей.
     */
    public Person() {}

    /**
     * Конструктор с параметрами.
     * Создает объект Person с полными данными о человеке.
     *
     * @param name        имя человека (не может быть пустым или null)
     * @param weight      вес человека (должен быть больше 0)
     * @param passportID идентификатор паспорта (может быть null, либо длина >= 8)
     * @param nationality национальность человека (не может быть null)
     * @param location    местоположение человека (не может быть null)
     * @throws IllegalArgumentException если name пустое или null, weight &lt;= 0,
      *                                   passportID не null и длина &lt; 8
     * @throws NullPointerException если nationality или location равны null
     */
    public Person(
        String name,
        double weight,
        String passportID,
        Country nationality,
        Location location
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Person.name cannot be empty or null"
            );
        }
        this.name = name;
        if (weight <= 0) {
            throw new IllegalArgumentException(
                "Person.weight must be greater than zero"
            );
        }
        this.weight = weight;
        if (passportID != null && passportID.length() < 8) {
            throw new IllegalArgumentException(
                "Person.passportID must be a string with length greater then or equal to 8 or null"
            );
        }
        this.passportID = passportID;
        this.nationality = Objects.requireNonNull(
            nationality,
            "Person.nationality cannot be null"
        );
        this.location = Objects.requireNonNull(
            location,
            "Person.location cannot be null"
        );
    }

    /**
     * Возвращает местоположение человека.
     *
     * @return объект Location, представляющий местоположение человека
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * Возвращает национальность человека.
     *
     * @return объект Country, представляющий национальность человека
     */
    public Country getNationality() {
        return this.nationality;
    }

    /**
     * Возвращает идентификатор паспорта.
     *
     * @return строку с идентификатором паспорта, может быть null
     */
    public String getPassportID() {
        return this.passportID;
    }

    /**
     * Возвращает вес человека.
     *
     * @return вес человека в виде числа с плавающей точкой
     */
    public double getWeight() {
        return this.weight;
    }

    /**
     * Возвращает имя человека.
     *
     * @return строку с именем человека, может быть null
     */
    public String getName() {
        return this.name;
    }

    /**
     * Устанавливает имя человека.
     *
     * @param name имя человека (не может быть пустым или null)
     * @throws IllegalArgumentException если name пустое или null
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Person.name cannot be empty or null"
            );
        }
        this.name = name;
    }

    /**
     * Устанавливает вес человека.
     *
     * @param weight вес человека (должен быть больше 0)
     * @throws IllegalArgumentException если weight &lt;= 0
     */
    public void setWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException(
                "Person.weight must be greater than zero"
            );
        }
        this.weight = weight;
    }

    /**
     * Устанавливает идентификатор паспорта.
     *
     * @param passportID идентификатор паспорта (может быть null, либо длина >= 8)
     * @throws IllegalArgumentException если passportID не null и длина &lt; 8
     */
    public void setPassportID(String passportID) {
        if (passportID != null && passportID.length() < 8) {
            throw new IllegalArgumentException(
                "Person.passportID must be a string with length greater then or equal to 8 or null"
            );
        }
        this.passportID = passportID;
    }

    /**
     * Устанавливает национальность человека.
     *
     * @param nationality национальность человека (не может быть null)
     * @throws NullPointerException если nationality равен null
     */
    public void setNationality(Country nationality) {
        this.nationality = Objects.requireNonNull(
            nationality,
            "Person.nationality cannot be null"
        );
    }

    /**
     * Устанавливает местоположение человека.
     *
     * @param location местоположение человека (не может быть null)
     * @throws NullPointerException если location равен null
     */
    public void setLocation(Location location) {
        this.location = Objects.requireNonNull(
            location,
            "Person.location cannot be null"
        );
    }

    /**
     * Возвращает строковое представление объекта Person.
     *
     * @return строку, содержащую все поля объекта Person
     */
    @Override
    public String toString() {
        return (
            "Person{" +
            "name='" +
            unescapeXml(name) +
            '\'' +
            ", weight=" +
            weight +
            ", passportID='" +
            unescapeXml(passportID) +
            '\'' +
            ", nationality=" +
            nationality +
            ", location=" +
            location +
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
     * Проверяет равенство данного объекта с другим объектом.
     * Два объекта Person равны, если все их поля совпадают.
     *
     * @param o объект для сравнения
     * @return true если объекты равны, false в противном случае
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Double.compare(person.weight, weight) == 0 &&
            Objects.equals(name, person.name) &&
            Objects.equals(passportID, person.passportID) &&
            nationality == person.nationality &&
            Objects.equals(location, person.location);
    }

    /**
     * Возвращает хеш-код объекта Person.
     *
     * @return хеш-код, вычисленный на основе всех полей объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, weight, passportID, nationality, location);
    }
}
