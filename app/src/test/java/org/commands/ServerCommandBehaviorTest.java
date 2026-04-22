package org.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.TreeMap;
import org.CollectionManager;
import org.dataclasses.Coordinates;
import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerCommandBehaviorTest {

    private ServerContext serverContext;

    @BeforeEach
    void setUp() {
        CollectionManager manager = CollectionManager.getInstance();
        TreeMap<String, Movie> collection = new TreeMap<>();
        collection.put(
            "alpha",
            new Movie(
                "Alien",
                new Coordinates(1, 2),
                1,
                MovieGenre.ACTION,
                MpaaRating.PG,
                null
            )
        );
        collection.put(
            "omega",
            new Movie(
                "Arrival",
                new Coordinates(3, 4),
                2,
                MovieGenre.ACTION,
                MpaaRating.R,
                null
            )
        );
        manager.setCollection(collection);
        serverContext = new ServerContext(manager, Path.of("/tmp/test.xml"));
    }

    @Test
    void removeGreaterKeyUsesStreamCompatibleBehavior() {
        RemoveGreaterKeyCommand command = new RemoveGreaterKeyCommand("beta");

        String result = command.exec(serverContext);

        assertEquals("removed 1 elements with keys greater than beta", result);
        assertEquals(1, serverContext.collectionManager().size());
    }

    @Test
    void updateCommandChangesExistingMovieFields() {
        UpdateCommand command = new UpdateCommand(
            serverContext.collectionManager().get("alpha").getId(),
            new Movie(
                "Aliens",
                new Coordinates(8, 9),
                5,
                MovieGenre.ACTION,
                MpaaRating.PG_13,
                null
            )
        );

        String result = command.exec(serverContext);

        assertEquals("element alpha successfully updated", result);
        assertEquals(
            "Aliens",
            serverContext.collectionManager().get("alpha").getName()
        );
        assertEquals(5, serverContext.collectionManager().get("alpha").getOscarsCount());
        assertEquals(
            MpaaRating.PG_13,
            serverContext.collectionManager().get("alpha").getMpaaRating()
        );
    }
}
