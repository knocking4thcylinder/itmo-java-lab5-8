package org;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.auth.PasswordHasher;
import org.commands.Command;
import org.commands.InputParser;
import org.commands.ScannerInputSource;
import org.commands.ServerCommand;
import org.commands.ServerConsoleCommandFactory;
import org.commands.ServerContext;
import org.commands.ServerCommandInvoker;
import org.commands.SharedCommand;
import org.commands.SubscribeUpdatesCommand;
import org.db.DatabaseConnector;
import org.db.LoadedMovies;
import org.db.MovieRepository;
import org.db.SchemaInitializer;
import org.db.UserRepository;
import org.shared.CommandRequest;
import org.shared.CommandResponse;
import org.shared.TransportCodec;
import org.shared.AuthResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        MovieRepository movieRepository = new MovieRepository(databaseConnector);
        PasswordHasher passwordHasher = new PasswordHasher();
        Map<String, String> ownersByKey = initializeDatabase(
            databaseConnector,
            collectionManager,
            movieRepository,
            passwordHasher
        );
        UserRepository userRepository = new UserRepository(databaseConnector);

        ServerContext serverContext = new ServerContext(
            collectionManager,
            movieRepository,
            userRepository,
            passwordHasher,
            ownersByKey,
            INTERNAL_OWNER_LOGIN
        );
        ServerCommandInvoker commandInvoker = new ServerCommandInvoker();
        UpdateBroadcaster updateBroadcaster = new UpdateBroadcaster();
        ServerContext serverConsoleContext = new ServerContext(
            collectionManager,
            movieRepository,
            userRepository,
            passwordHasher,
            ownersByKey,
            INTERNAL_OWNER_LOGIN
        );
        startConsole(commandInvoker, serverConsoleContext);

        try (
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ExecutorService requestReaderPool = Executors.newCachedThreadPool(
                namedThreadFactory("server-request-reader")
            );
            ExecutorService requestProcessorPool = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                namedThreadFactory("server-request-processor")
            );
            ForkJoinPool responsePool = new ForkJoinPool()
        ) {
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(true);
            LOGGER.info("Server started on port {}", port);
            while (true) {
                SocketChannel clientChannel = serverChannel.accept();
                LOGGER.info("Accepted connection from {}", describeChannel(clientChannel));
                requestReaderPool.submit(
                    () -> readClientRequest(
                        clientChannel,
                        commandInvoker,
                        serverContext,
                        updateBroadcaster,
                        requestProcessorPool,
                        responsePool
                    )
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start server", e);
        }
    }

    private static Map<String, String> initializeDatabase(
        DatabaseConnector databaseConnector,
        CollectionManager collectionManager,
        MovieRepository movieRepository,
        PasswordHasher passwordHasher
    ) {
        try {
            LOGGER.info("Initializing database schema");
            new SchemaInitializer(databaseConnector).initialize();
            new org.db.UserRepository(databaseConnector).create(
                INTERNAL_OWNER_LOGIN,
                passwordHasher.hash("")
            );
            LOGGER.info("Loading collection from database");
            LoadedMovies loadedMovies = movieRepository.loadAllWithOwners();
            collectionManager.setCollection(loadedMovies.movies());
            LOGGER.info(
                "Loaded {} movies from database",
                collectionManager.size()
            );
            return new ConcurrentHashMap<>(loadedMovies.ownersByKey());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private static void readClientRequest(
        SocketChannel clientChannel,
        ServerCommandInvoker commandInvoker,
        ServerContext baseServerContext,
        UpdateBroadcaster updateBroadcaster,
        ExecutorService requestProcessorPool,
        ForkJoinPool responsePool
    ) {
        try {
            clientChannel.configureBlocking(true);
            LOGGER.info("Started handling {}", describeChannel(clientChannel));
            Object requestObject = TransportCodec.decode(readPayload(clientChannel));
            requestProcessorPool.submit(
                () -> processRequestAndRespond(
                    requestObject,
                    clientChannel,
                    commandInvoker,
                    baseServerContext,
                    updateBroadcaster,
                    responsePool
                )
            );
        } catch (EOFException ignored) {
            LOGGER.warn(
                "Client {} closed the connection before sending a full request",
                describeChannel(clientChannel)
            );
            closeClientChannel(clientChannel);
        } catch (Exception e) {
            LOGGER.error(
                "Failed to handle client {}",
                describeChannel(clientChannel),
                e
            );
            closeClientChannel(clientChannel);
        }
    }

    private static void processRequestAndRespond(
        Object requestObject,
        SocketChannel clientChannel,
        ServerCommandInvoker commandInvoker,
        ServerContext baseServerContext,
        UpdateBroadcaster updateBroadcaster,
        ForkJoinPool responsePool
    ) {
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
                if (request.command().requiresAuthentication()) {
                    validateRequestCredentials(request, baseServerContext);
                }
                if (request.command() instanceof SubscribeUpdatesCommand) {
                    updateBroadcaster.subscribe(clientChannel);
                    writeResponse(clientChannel, CommandResponse.success("subscribed"));
                    LOGGER.info("Registered update subscriber {}", describeChannel(clientChannel));
                    return;
                }
                ServerContext requestContext = contextForRequest(
                    request,
                    baseServerContext
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
                response = successResponse(result, requestContext);
                if (shouldBroadcastUpdate(request.command())) {
                    updateBroadcaster.broadcast();
                }
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
        CommandResponse responseToSend = response;
        responsePool.submit(() -> {
            try {
                writeResponse(clientChannel, responseToSend);
                LOGGER.info("Sent response to {}", describeChannel(clientChannel));
            } catch (IOException e) {
                LOGGER.error("Failed to send response to {}", describeChannel(clientChannel), e);
            } finally {
                closeClientChannel(clientChannel);
            }
        }).join();
    }

    private static boolean shouldBroadcastUpdate(SharedCommand command) {
        return command.requiresAuthentication() && !command.isReadOnly();
    }

    private static ServerContext contextForRequest(
        CommandRequest request,
        ServerContext baseServerContext
    ) {
        String ownerLogin = request.login() == null
            ? INTERNAL_OWNER_LOGIN
            : request.login();
        return new ServerContext(
            baseServerContext.collectionManager(),
            baseServerContext.movieRepository(),
            baseServerContext.userRepository(),
            baseServerContext.passwordHasher(),
            baseServerContext.ownersByKey(),
            ownerLogin
        );
    }

    private static void validateRequestCredentials(
        CommandRequest request,
        ServerContext baseServerContext
    ) throws Exception {
        if (request.login() == null || request.password() == null) {
            throw new IllegalStateException("authentication required");
        }
        String storedHash = baseServerContext.userRepository()
            .findPasswordHash(request.login())
            .orElseThrow(() -> new IllegalStateException("invalid login or password"));
        if (!baseServerContext.passwordHasher().verify(request.password(), storedHash)) {
            throw new IllegalStateException("invalid login or password");
        }
    }

    private static CommandResponse successResponse(
        String message,
        ServerContext requestContext
    ) {
        AuthResult authResult = requestContext.latestAuthResult();
        if (authResult == null) {
            return CommandResponse.success(message);
        }
        return CommandResponse.authenticated(
            message,
            authResult.login()
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
            InputParser inputParser = new InputParser(
                new ScannerInputSource(System.in)
            );
            ServerConsoleCommandFactory commandFactory =
                new ServerConsoleCommandFactory(inputParser);
            System.out.print("server> ");
            for (String[] commandLine : inputParser) {
                if (commandLine.length == 0 || commandLine[0].isEmpty()) {
                    System.out.print("server> ");
                    continue;
                }
                try {
                    if ("exit".equals(commandLine[0])) {
                        LOGGER.info("Server shutdown requested from console");
                        System.exit(0);
                    }
                    Command command = commandFactory.create(
                        commandLine[0],
                        java.util.Arrays.copyOfRange(
                            commandLine,
                            1,
                            commandLine.length
                        )
                    );
                    String result;
                    if (command instanceof ServerCommand serverCommand) {
                        result = commandInvoker.invoke(serverCommand, serverContext);
                    } else if (command instanceof SharedCommand sharedCommand) {
                        result = commandInvoker.invoke(sharedCommand, serverContext);
                    } else {
                        throw new IllegalStateException(
                            "Unsupported server console command type"
                        );
                    }
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

    private static String describeChannel(SocketChannel channel) {
        try {
            return String.valueOf(channel.getRemoteAddress());
        } catch (IOException e) {
            return "<unknown-client>";
        }
    }

    private static ThreadFactory namedThreadFactory(String namePrefix) {
        AtomicInteger counter = new AtomicInteger(1);
        return task -> new Thread(
            task,
            namePrefix + "-" + counter.getAndIncrement()
        );
    }

    private static void closeClientChannel(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.warn("Failed to close client channel", e);
        }
    }

    private static final class UpdateBroadcaster {
        private final java.util.Set<SocketChannel> subscribers = ConcurrentHashMap.newKeySet();

        private void subscribe(SocketChannel channel) {
            subscribers.add(channel);
        }

        private void broadcast() {
            CommandResponse update = CommandResponse.success("collection-updated");
            for (SocketChannel subscriber : subscribers) {
                try {
                    synchronized (subscriber) {
                        writeResponse(subscriber, update);
                    }
                } catch (IOException e) {
                    subscribers.remove(subscriber);
                    closeClientChannel(subscriber);
                }
            }
        }
    }
}
