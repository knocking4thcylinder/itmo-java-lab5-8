package org.client;

import org.client.io.InputParser;
import org.client.io.ScannerInputSource;

public final class ClientApp {

    private ClientApp() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println(
                "Usage: ./gradlew :client:run --args='<host> <port>'"
            );
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        try (
            InputParser inputParser = new InputParser(
                new ScannerInputSource(System.in)
            )
        ) {
            ClientCommandRunner runner = new ClientCommandRunner(
                host,
                port,
                inputParser,
                new NonBlockingRequestClient()
            );
            runner.runInteractive();
        }
    }
}
