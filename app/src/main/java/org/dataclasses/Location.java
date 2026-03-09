package org.dataclasses;

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
        if (name != null && name.equals("")) {
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
}
