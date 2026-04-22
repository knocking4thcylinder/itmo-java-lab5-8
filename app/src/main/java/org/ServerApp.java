package org;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.commands.CommandInvoker;
import org.commands.ServerContext;
import org.shared.CommandRequest;
import org.shared.CommandResponse;

/**
 * Серверное приложение для выполнения команд над коллекцией.
 */
public class ServerApp {

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
        CommandInvoker commandInvoker = new CommandInvoker();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(
                "Server started on port " + port + ", storage: " + inputPath
            );
            while (true) {
                handleClient(serverSocket.accept(), commandInvoker, serverContext);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start server", e);
        }
    }

    private static void handleClient(
        Socket socket,
        CommandInvoker commandInvoker,
        ServerContext serverContext
    ) {
        try (socket) {
            ObjectOutputStream outputStream = new ObjectOutputStream(
                socket.getOutputStream()
            );
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(
                socket.getInputStream()
            );

            Object requestObject = inputStream.readObject();
            CommandResponse response;
            if (!(requestObject instanceof CommandRequest request)) {
                response = CommandResponse.failure("Unsupported request type");
            } else {
                try {
                    String result = commandInvoker.invoke(
                        request.command(),
                        serverContext
                    );
                    response = CommandResponse.success(result);
                } catch (Exception e) {
                    response = CommandResponse.failure(e.getMessage());
                }
            }
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (EOFException ignored) {
            // Клиент закрыл соединение до отправки запроса.
        } catch (Exception e) {
            System.err.println("Failed to handle client: " + e.getMessage());
        }
    }
}
