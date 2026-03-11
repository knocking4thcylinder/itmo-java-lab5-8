package org.dataclasses;

import java.util.Objects;

public class Location {

    private long x;
    private double y;
    private String name;

    public Location() {}

    public Location(long x, double y, String name) {
        this.x = x;
        this.y = y;
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Location.name cannot be empty");
        }
        this.name = name;
    }

    public void setX(long x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setName(String name) {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Location.name cannot be empty");
        }
        this.name = name;
    }

    public long getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return (
            "Location{" + "x=" + x + ", y=" + y + ", name='" + name + '\'' + '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return x == location.x &&
            Double.compare(location.y, y) == 0 &&
            Objects.equals(name, location.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, name);
    }
}
