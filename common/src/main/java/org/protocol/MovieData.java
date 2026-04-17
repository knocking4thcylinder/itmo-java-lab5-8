package org.protocol;

import java.io.Serial;
import java.io.Serializable;
import org.dataclasses.Coordinates;
import org.dataclasses.Person;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;

public class MovieData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private Coordinates coordinates;
    private int oscarsCount;
    private MovieGenre genre;
    private MpaaRating mpaaRating;
    private Person operator;

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public int getOscarsCount() {
        return oscarsCount;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public Person getOperator() {
        return operator;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setOscarsCount(int oscarsCount) {
        this.oscarsCount = oscarsCount;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public void setOperator(Person operator) {
        this.operator = operator;
    }
}
