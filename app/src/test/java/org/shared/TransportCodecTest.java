package org.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.ByteBuffer;
import org.commands.InsertCommand;
import org.dataclasses.Coordinates;
import org.dataclasses.Movie;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;
import org.junit.jupiter.api.Test;

class TransportCodecTest {

    @Test
    void commandRequestRoundTripsThroughCodec() throws Exception {
        Movie movie = new Movie(
            "Blade Runner",
            new Coordinates(10, 20),
            2,
            MovieGenre.ACTION,
            MpaaRating.PG,
            null
        );
        CommandRequest request = CommandRequest.of(
            new InsertCommand("neo", movie)
        );

        ByteBuffer encoded = TransportCodec.encode(request);
        encoded.getInt();
        byte[] payload = new byte[encoded.remaining()];
        encoded.get(payload);

        Object decoded = TransportCodec.decode(payload);
        CommandRequest decodedRequest = assertInstanceOf(
            CommandRequest.class,
            decoded
        );
        InsertCommand insertCommand = assertInstanceOf(
            InsertCommand.class,
            decodedRequest.command()
        );
        assertEquals("element neo successfully inserted", insertCommand.exec(
            new org.commands.ServerContext(
                org.CollectionManager.getInstance(),
                java.nio.file.Path.of("/tmp/test.xml")
            )
        ));
    }
}
