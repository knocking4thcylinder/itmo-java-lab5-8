package org.dataclasses;

import java.util.Objects;
import org.dataclasses.enums.Country;

public class Person {

    private String name;
    private double weight;
    private String passportID;
    private Country nationality;
    private Location location;

    public Person() {}

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

    public Location getLocation() {
        return this.location;
    }

    public Country getNationality() {
        return this.nationality;
    }

    public String getPassportID() {
        return this.passportID;
    }

    public double getWeight() {
        return this.weight;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Person.name cannot be empty or null"
            );
        }
        this.name = name;
    }

    public void setWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException(
                "Person.weight must be greater than zero"
            );
        }
        this.weight = weight;
    }

    public void setPassportID(String passportID) {
        if (passportID != null && passportID.length() < 8) {
            throw new IllegalArgumentException(
                "Person.passportID must be a string with length greater then or equal to 8 or null"
            );
        }
        this.passportID = passportID;
    }

    public void setNationality(Country nationality) {
        this.nationality = Objects.requireNonNull(
            nationality,
            "Person.nationality cannot be null"
        );
    }

    public void setLocation(Location location) {
        this.location = Objects.requireNonNull(
            location,
            "Person.location cannot be null"
        );
    }

    @Override
    public String toString() {
        return (
            "Person{" +
            "name='" +
            name +
            '\'' +
            ", weight=" +
            weight +
            ", passportID='" +
            passportID +
            '\'' +
            ", nationality=" +
            nationality +
            ", location=" +
            location +
            '}'
        );
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(name, weight, passportID, nationality, location);
    }
}
