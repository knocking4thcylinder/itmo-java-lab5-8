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
}
