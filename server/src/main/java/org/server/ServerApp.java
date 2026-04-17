package org.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.server.collection.CollectionManager;
import org.server.collection.XmlCollectionStorage;
import org.server.command.CommandProcessor;
import org.server.console.AdminConsole;
import org.server.network.ConnectionAcceptor;
import org.server.network.RequestReader;
import org.server.network.ResponseSender;

public final class ServerApp {

    private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);

    private ServerApp() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            logger.error(
                "Usage: ./gradlew :server:run --args='<port> <collection-file>'"
            );
            return;
        }

        int port = Integer.parseInt(args[0]);
        Path collectionPath = Paths.get(args[1]);
        XmlCollectionStorage storage = new XmlCollectionStorage(collectionPath);
        CollectionManager collectionManager = new CollectionManager();
        collectionManager.replaceAll(storage.load());
        logger.info(
            "Loaded {} collection entries from {}",
            collectionManager.size(),
            collectionPath
        );

        ServerRuntime runtime = new ServerRuntime(
            new ConnectionAcceptor(port, 250),
            new RequestReader(),
            new ResponseSender(),
            new CommandProcessor(collectionManager),
            storage,
            collectionManager,
            new AdminConsole()
        );

        Runtime.getRuntime()
            .addShutdownHook(new Thread(runtime::saveCollectionQuietly));

        runtime.run();
    }
}
