package org;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import org.auth.SessionManager;
import org.commands.ServerCommand;
import org.commands.ServerContext;
import org.commands.ServerCommandInvoker;
import org.db.DatabaseConnector;
import org.db.MovieRepository;
import org.db.SchemaInitializer;
import org.shared.CommandRequest;
import org.shared.CommandResponse;
import org.shared.TransportCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;

/**
 * Серверное приложение для выполнения команд над коллекцией.
 */
public class ServerApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerApp.class);
    private static final String INTERNAL_OWNER_LOGIN = "__server__";

    /**
     * Точка входа серверного приложения.
     *
     * @param args порт сервера
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java org.ServerApp <port>");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("\"" + args[0] + "\" is not a valid port");
            System.exit(1);
            return;
        }

        CollectionManager collectionManager = CollectionManager.getInstance();
        DatabaseConnector databaseConnector = new DatabaseConnector();
        MovieRepository movieRepository = initializeDatabase(
            databaseConnector,
            collectionManager
        );
        SessionManager sessionManager = new SessionManager();

        ServerContext serverContext = new ServerContext(
            collectionManager,
            movieRepository,
            INTERNAL_OWNER_LOGIN
        );
        ServerCommandInvoker commandInvoker = new ServerCommandInvoker();
        startConsole(commandInvoker, serverContext);

        try (
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open()
        ) {
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info("Server started on port {}", port);
            while (true) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        acceptClients(serverChannel, selector);
                    } else if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        key.cancel();
                        handleClient(
                            clientChannel,
                            commandInvoker,
                            serverContext,
                            sessionManager
                        );
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start server", e);
        }
    }

    private static MovieRepository initializeDatabase(
        DatabaseConnector databaseConnector,
        CollectionManager collectionManager
    ) {
        try {
            LOGGER.info("Initializing database schema");
            new SchemaInitializer(databaseConnector).initialize();
            new org.db.UserRepository(databaseConnector).create(
                INTERNAL_OWNER_LOGIN,
                ""
            );
            MovieRepository movieRepository = new MovieRepository(databaseConnector);
            LOGGER.info("Loading collection from database");
            collectionManager.setCollection(movieRepository.loadAll());
            LOGGER.info(
                "Loaded {} movies from database",
                collectionManager.size()
            );
            return movieRepository;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private static void acceptClients(
        ServerSocketChannel serverChannel,
        Selector selector
    ) throws IOException {
        SocketChannel clientChannel;
        while ((clientChannel = serverChannel.accept()) != null) {
            LOGGER.info("Accepted connection from {}", describeChannel(clientChannel));
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        }
    }

    private static void handleClient(
        SocketChannel clientChannel,
        ServerCommandInvoker commandInvoker,
        ServerContext baseServerContext,
        SessionManager sessionManager
    ) {
        try (clientChannel) {
            clientChannel.configureBlocking(true);
            LOGGER.info("Started handling {}", describeChannel(clientChannel));
            Object requestObject = TransportCodec.decode(readPayload(clientChannel));
            CommandResponse response;
            if (!(requestObject instanceof CommandRequest request)) {
                LOGGER.warn(
                    "Received unsupported request type from {}: {}",
                    describeChannel(clientChannel),
                    requestObject.getClass().getName()
                );
                response = CommandResponse.failure("Unsupported request type");
            } else {
                try {
                    LOGGER.info(
                        "Executing {} from {}",
                        request.command().getClass().getSimpleName(),
                        describeChannel(clientChannel)
                    );
                    ServerContext requestContext = contextForRequest(
                        request,
                        baseServerContext,
                        sessionManager
                    );
                    String result = commandInvoker.invoke(
                        request.command(),
                        requestContext
                    );
                    LOGGER.info(
                        "Completed {} from {} with success",
                        request.command().getClass().getSimpleName(),
                        describeChannel(clientChannel)
                    );
                    response = CommandResponse.success(result);
                } catch (Exception e) {
                    LOGGER.warn(
                        "Command {} from {} failed: {}",
                        request.command().getClass().getSimpleName(),
                        describeChannel(clientChannel),
                        e.getMessage()
                    );
                    response = CommandResponse.failure(e.getMessage());
                }
            }
            writeResponse(clientChannel, response);
            LOGGER.info("Sent response to {}", describeChannel(clientChannel));
        } catch (EOFException ignored) {
            LOGGER.warn(
                "Client {} closed the connection before sending a full request",
                describeChannel(clientChannel)
            );
        } catch (Exception e) {
            LOGGER.error(
                "Failed to handle client {}",
                describeChannel(clientChannel),
                e
            );
        }
    }

    private static ServerContext contextForRequest(
        CommandRequest request,
        ServerContext baseServerContext,
        SessionManager sessionManager
    ) {
        String ownerLogin = sessionManager
            .findLogin(request.authToken())
            .orElse(INTERNAL_OWNER_LOGIN);
        return new ServerContext(
            baseServerContext.collectionManager(),
            baseServerContext.movieRepository(),
            ownerLogin
        );
    }

    private static byte[] readPayload(SocketChannel channel) throws IOException {
        DataInputStream inputStream = new DataInputStream(
            Channels.newInputStream(channel)
        );
        int payloadLength = inputStream.readInt();
        byte[] payload = new byte[payloadLength];
        inputStream.readFully(payload);
        return payload;
    }

    private static void writeResponse(
        SocketChannel channel,
        CommandResponse response
    )
        throws IOException {
        byte[] encodedResponse = TransportCodec.encode(response).array();
        DataOutputStream outputStream = new DataOutputStream(
            Channels.newOutputStream(channel)
        );
        outputStream.write(encodedResponse);
        outputStream.flush();
    }

    private static void startConsole(
        ServerCommandInvoker commandInvoker,
        ServerContext serverContext
    ) {
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.print("server> ");
            while (scanner.hasNextLine()) {
                String[] commandLine = scanner.nextLine().trim().split(" ");
                if (commandLine.length == 0 || commandLine[0].isEmpty()) {
                    System.out.print("server> ");
                    continue;
                }
                try {
                    if ("exit".equals(commandLine[0])) {
                        LOGGER.info("Server shutdown requested from console");
                        System.exit(0);
                    }
                    ServerCommand serverCommand = createServerCommand(commandLine);
                    String result = commandInvoker.invoke(serverCommand, serverContext);
                    if (result != null && !result.isEmpty()) {
                        System.out.println(result);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                System.out.print("server> ");
            }
        }, "server-console");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    private static ServerCommand createServerCommand(String[] commandLine) {
        return switch (commandLine[0]) {
            case "help" -> {
                requireArgCount(commandLine, 0);
                yield new org.commands.ServerHelpCommand();
            }
            default -> throw new IllegalArgumentException(
                "No server command with name \"" + commandLine[0] + "\" exists"
            );
        };
    }

    private static void requireArgCount(String[] commandLine, int count) {
        int actualCount = commandLine.length - 1;
        if (actualCount != count) {
            throw new IllegalArgumentException(
                "command \"" +
                commandLine[0] +
                "\" accepts exactly " +
                count +
                " argument" +
                (count == 1 ? "" : "s")
            );
        }
    }

    private static String describeChannel(SocketChannel channel) {
        try {
            return String.valueOf(channel.getRemoteAddress());
        } catch (IOException e) {
            return "<unknown-client>";
        }
    }
}
