package org.dataclasses;

import java.io.Serializable;
import java.util.Objects;

/**
 * Класс для хранения местоположения.
 * Представляет собой трехмерные координаты с названием места.
 */
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    private long x;
    private double y;
    private String name;

    /**
     * Конструктор по умолчанию.
     * Создает объект Location с неопределенными значениями полей.
     */
    public Location() {}

    /**
     * Конструктор с параметрами.
     * Создает объект Location с заданными координатами и названием.
     *
     * @param x    координата по оси X
     * @param y    координата по оси Y
     * @param name название места (может быть null, но не может быть пустым)
     * @throws IllegalArgumentException если name не null и является пустой строкой
     */
    public Location(long x, double y, String name) {
        this.x = x;
        this.y = y;
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Location.name cannot be empty");
        }
        this.name = name;
    }

    /**
     * Устанавливает значение координаты x.
     *
     * @param x координата по оси X
     */
    public void setX(long x) {
        this.x = x;
    }

    /**
     * Устанавливает значение координаты y.
     *
     * @param y координата по оси Y
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Устанавливает название места.
     *
     * @param name название места (может быть null, но не может быть пустым)
     * @throws IllegalArgumentException если name не null и является пустой строкой
     */
    public void setName(String name) {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Location.name cannot be empty");
        }
        this.name = name;
    }

    /**
     * Возвращает значение координаты x.
     *
     * @return координата по оси x
     */
    public long getX() {
        return this.x;
    }

    /**
     * Возвращает значение координаты y.
     *
     * @return координата по оси y
     */
    public double getY() {
        return this.y;
    }

    /**
     * Возвращает название места.
     *
     * @return строку с названием места, может быть null
     */
    public String getName() {
        return this.name;
    }

    /**
     * Возвращает строковое представление объекта Location.
     *
     * @return строку, содержащую значения координат x, y и название места
     */
    @Override
    public String toString() {
        return (
            "Location{" + "x=" + x + ", y=" + y + ", name='" + unescapeXml(name) + '\'' + '}'
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
     * Два объекта Location равны, если их координаты и название совпадают.
     *
     * @param o объект для сравнения
     * @return true если объекты равны, false в противном случае
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return x == location.x &&
            Double.compare(location.y, y) == 0 &&
            Objects.equals(name, location.name);
    }

    /**
     * Возвращает хеш-код объекта Location.
     *
     * @return хеш-код, вычисленный на основе координат x, y и названия
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, name);
    }
}
