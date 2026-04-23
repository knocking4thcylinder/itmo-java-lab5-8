package org;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import org.commands.Command;
import org.commands.CommandFactory;
import org.commands.InputParser;
import org.commands.SaveCommand;
import org.commands.ScannerInputSource;
import org.commands.ServerContext;
import org.commands.ServerCommand;
import org.commands.ServerCommandInvoker;
import org.shared.CommandRequest;
import org.shared.CommandResponse;
import org.shared.TransportCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Серверное приложение для выполнения команд над коллекцией.
 */
public class ServerApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerApp.class);

    /**
     * Точка входа серверного приложения.
     *
     * @param args порт и путь к XML-файлу коллекции
     * @throws FileNotFoundException если файл коллекции не найден
     * @throws AccessDeniedException если к файлу нет доступа
     */
    public static void main(String[] args)
        throws FileNotFoundException, AccessDeniedException {
        if (args.length != 2) {
            System.out.println(
                "Usage: java org.ServerApp <port> <path/to/inputfile.xml>"
            );
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

        Path inputPath = Paths.get(args[1]);
        CollectionManager collectionManager = CollectionManager.getInstance();
        collectionManager.setCollection(CollectionLoader.load(inputPath));
        ServerContext serverContext = new ServerContext(collectionManager, inputPath);
        ServerCommandInvoker commandInvoker = new ServerCommandInvoker();
        registerShutdownHook(commandInvoker, serverContext);
        startConsole(commandInvoker, serverContext);

        try (
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open()
        ) {
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info(
                "Server started on port " + port + ", storage: " + inputPath
            );
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
                        handleClient(clientChannel, commandInvoker, serverContext);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start server", e);
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
        ServerContext serverContext
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
            } else if (request.command() instanceof SaveCommand) {
                LOGGER.warn(
                    "Rejected remote server-only command SaveCommand from {}",
                    describeChannel(clientChannel)
                );
                response = CommandResponse.failure("save is server-only");
            } else {
                try {
                    LOGGER.info(
                        "Executing {} from {}",
                        request.command().getClass().getSimpleName(),
                        describeChannel(clientChannel)
                    );
                    String result = commandInvoker.invoke(
                        request.command(),
                        serverContext
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
            CommandFactory commandFactory = new CommandFactory(
                inputParser,
                CommandFactory.Environment.SERVER
            );
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
                        Arrays.copyOfRange(commandLine, 1, commandLine.length)
                    );
                    if (!(command instanceof ServerCommand serverCommand)) {
                        throw new IllegalStateException(
                            "Only server commands are available in server console"
                        );
                    }
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

    private static void registerShutdownHook(
        ServerCommandInvoker commandInvoker,
        ServerContext serverContext
    ) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                String result = commandInvoker.invoke(new SaveCommand(), serverContext);
                LOGGER.info("Shutdown save completed: {}", result);
            } catch (Exception e) {
                LOGGER.error("Shutdown save failed", e);
            }
        }, "server-shutdown-hook"));
    }

    private static String describeChannel(SocketChannel channel) {
        try {
            return String.valueOf(channel.getRemoteAddress());
        } catch (IOException e) {
            return "<unknown-client>";
        }
    }
}
