package org.dataclasses;

import java.util.Objects;

public class Coordinates {

    private Integer x;
    private Integer y;

    public Coordinates() {}

    public Coordinates(Integer x, Integer y) {
        this.y = Objects.requireNonNull(y, "Coordinates.Y can not be null");
        this.x = Objects.requireNonNull(x, "Coordinates.X can not be null");
    }

    public void setX(Integer x) {
        this.x = Objects.requireNonNull(x, "Coordinates.X can not be null");
    }

    public void setY(Integer y) {
        this.y = Objects.requireNonNull(y, "Coordinates.Y can not be null");
    }

    public Integer getX() {
        return this.x;
    }

    public Integer getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "Coordinates{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
