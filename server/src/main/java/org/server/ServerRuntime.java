package org.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import org.protocol.CommandRequest;
import org.protocol.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.server.collection.CollectionManager;
import org.server.collection.XmlCollectionStorage;
import org.server.command.CommandProcessor;
import org.server.console.AdminConsole;
import org.server.network.ConnectionAcceptor;
import org.server.network.RequestReader;
import org.server.network.ResponseSender;

public final class ServerRuntime {

    private static final Logger logger = LoggerFactory.getLogger(ServerRuntime.class);

    private final ConnectionAcceptor connectionAcceptor;
    private final RequestReader requestReader;
    private final ResponseSender responseSender;
    private final CommandProcessor commandProcessor;
    private final XmlCollectionStorage storage;
    private final CollectionManager collectionManager;
    private final AdminConsole adminConsole;

    private boolean running = true;

    public ServerRuntime(
        ConnectionAcceptor connectionAcceptor,
        RequestReader requestReader,
        ResponseSender responseSender,
        CommandProcessor commandProcessor,
        XmlCollectionStorage storage,
        CollectionManager collectionManager,
        AdminConsole adminConsole
    ) {
        this.connectionAcceptor = connectionAcceptor;
        this.requestReader = requestReader;
        this.responseSender = responseSender;
        this.commandProcessor = commandProcessor;
        this.storage = storage;
        this.collectionManager = collectionManager;
        this.adminConsole = adminConsole;
    }

    public void run() throws IOException {
        logger.info("Server started. Admin commands: help, save, exit.");
        while (running) {
            handleAdminCommand();
            Optional<Socket> maybeSocket = connectionAcceptor.accept();
            if (maybeSocket.isEmpty()) {
                continue;
            }

            try (Socket socket = maybeSocket.get()) {
                logger.info(
                    "Accepted connection from {}",
                    socket.getRemoteSocketAddress()
                );
                CommandResponse response;
                try {
                    CommandRequest request = requestReader.read(socket);
                    logger.info("Received request: {}", request.getCommandType());
                    response = commandProcessor.process(request);
                    logger.info("Sent response for {}", request.getCommandType());
                } catch (Exception exception) {
                    logger.warn("Failed to process request", exception);
                    response = CommandResponse.failure(exception.getMessage());
                }
                responseSender.write(socket, response);
            } catch (Exception exception) {
                logger.error("Failed to handle client request", exception);
            }
        }

        saveCollectionQuietly();
        connectionAcceptor.close();
    }

    public void saveCollectionQuietly() {
        try {
            storage.save(collectionManager.snapshot());
            logger.info("Collection saved to {}", storage.getPath());
        } catch (Exception exception) {
            logger.error("Failed to save collection", exception);
        }
    }

    private void handleAdminCommand() {
        try {
            String adminCommand = adminConsole.pollCommand();
            if (adminCommand == null || adminCommand.isBlank()) {
                return;
            }

            switch (adminCommand.trim()) {
                case "help" -> logger.info("Admin commands: help, save, exit");
                case "save" -> saveCollectionQuietly();
                case "exit" -> running = false;
                default -> logger.warn("Unknown admin command: {}", adminCommand);
            }
        } catch (IOException exception) {
            logger.error("Failed to read admin command", exception);
        }
    }
}
