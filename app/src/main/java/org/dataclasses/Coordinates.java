package org.dataclasses;

import java.io.Serializable;
import java.util.Objects;

/**
 * Класс для хранения координат (x, y).
 * Представляет собой пару целочисленных координат на плоскости.
 */
public class Coordinates implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer x;
    private Integer y;

    /**
     * Конструктор по умолчанию.
     * Создает объект Coordinates с неопределенными координатами.
     */
    public Coordinates() {}

    /**
     * Конструктор с параметрами.
     * Создает объект Coordinates с заданными координатами.
     *
     * @param x координата по оси X (не может быть null)
     * @param y координата по оси Y (не может быть null)
     * @throws NullPointerException если x или y равны null
     */
    public Coordinates(Integer x, Integer y) {
        this.y = Objects.requireNonNull(y, "Coordinates.Y can not be null");
        this.x = Objects.requireNonNull(x, "Coordinates.X can not be null");
    }

    /**
     * Устанавливает значение координаты x.
     *
     * @param x координата по оси X (не может быть null)
     * @throws NullPointerException если x равен null
     */
    public void setX(Integer x) {
        this.x = Objects.requireNonNull(x, "Coordinates.X can not be null");
    }

    /**
     * Устанавливает значение координаты y.
     *
     * @param y координата по оси Y (не может быть null)
     * @throws NullPointerException если y равен null
     */
    public void setY(Integer y) {
        this.y = Objects.requireNonNull(y, "Coordinates.Y can not be null");
    }

    /**
     * Возвращает значение координаты x.
     *
     * @return координата по оси x, может быть null
     */
    public Integer getX() {
        return this.x;
    }

    /**
     * Возвращает значение координаты y.
     *
     * @return координата по оси y, может быть null
     */
    public Integer getY() {
        return this.y;
    }

    /**
     * Возвращает строковое представление объекта Coordinates.
     *
     * @return строка, содержащая значения координат x и y
     */
    @Override
    public String toString() {
        return "Coordinates{" + "x=" + x + ", y=" + y + '}';
    }

    /**
     * Проверяет равенство данного объекта с другим объектом.
     * Два объекта Coordinates равны, если их координаты x и y совпадают.
     *
     * @param o объект для сравнения
     * @return true если объекты равны, false в противном случае
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    /**
     * Возвращает хеш-код объекта Coordinates.
     *
     * @return хеш-код, вычисленный на основе координат x и y
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
