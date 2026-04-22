package org.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;

class ClientContextNetworkTest {

    @Test
    void clientContextOnlyStoresInputParser() {
        InputParser inputParser = new InputParser(
            new ScannerInputSource(new ByteArrayInputStream(new byte[0]))
        );
        CommandFactory commandFactory = new CommandFactory(
            inputParser,
            CommandFactory.Environment.CLIENT
        );
        ClientCommandInvoker commandInvoker = new ClientCommandInvoker(
            "localhost",
            5555
        );
        ClientContext clientContext = new ClientContext(
            inputParser,
            commandFactory,
            commandInvoker
        );

        assertSame(inputParser, clientContext.inputParser());
        assertEquals(false, inputParser.iterator().hasNext());
    }

    @Test
    void unavailableServerProducesUserFriendlyError() {
        InputParser inputParser = new InputParser(
            new ScannerInputSource(new ByteArrayInputStream(new byte[0]))
        );
        CommandFactory commandFactory = new CommandFactory(
            inputParser,
            CommandFactory.Environment.CLIENT
        );
        ClientCommandInvoker commandInvoker = new ClientCommandInvoker(
            "127.0.0.1",
            1
        );
        ClientContext clientContext = new ClientContext(
            inputParser,
            commandFactory,
            commandInvoker
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> commandInvoker.invoke(new InfoCommand(), clientContext)
        );

        assertEquals(
            "Server is temporarily unavailable. Please try again later.",
            exception.getMessage()
        );
    }
}
