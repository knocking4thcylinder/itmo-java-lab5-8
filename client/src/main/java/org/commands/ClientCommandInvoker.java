package org.commands;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;
import java.util.StringJoiner;
import org.shared.CommandRequest;
import org.shared.CommandResponse;
import org.shared.TransportCodec;

/**
 * Выполняет клиентские команды и пересылает серверные команды на сервер.
 */
public class ClientCommandInvoker {

    private static final int MAX_RETRIES = 3;
    private static final int SELECT_TIMEOUT_MS = 2_000;
    private final boolean promptForMultiServerMutations;

    /**
     * Создает новый клиентский invoker.
     *
     * @param serverHost хост сервера
     * @param serverPort порт сервера
     */
    public ClientCommandInvoker(String serverHost, int serverPort) {
        this(serverHost, serverPort, true);
    }

    public ClientCommandInvoker(
        String serverHost,
        int serverPort,
        boolean promptForMultiServerMutations
    ) {
        Objects.requireNonNull(serverHost, "Server host cannot be null");
        this.promptForMultiServerMutations = promptForMultiServerMutations;
    }

    /**
     * Выполняет клиентскую или серверную команду.
     *
     * @param command команда
     * @param context клиентский контекст
     * @return результат выполнения
     * @throws Exception если выполнение завершилось с ошибкой
     */
    public String invoke(Command command, ClientContext context)
        throws Exception {
        if (command instanceof ClientCommand clientCommand) {
            return clientCommand.exec(context);
        }
        if (command instanceof SharedCommand sharedCommand) {
            return executeSharedCommand(sharedCommand, context);
        }
        throw new IllegalStateException(
            "Unsupported command type: " + command.getClass().getName()
        );
    }

    private String executeSharedCommand(
        SharedCommand sharedCommand,
        ClientContext context
    )
        throws Exception {
        if (sharedCommand.isReadOnly()) {
            return executeReadOnlyOnAllServers(sharedCommand, context);
        }

        ServerEndpoint activeEndpoint = context.activeEndpoint();
        String activeResult = executeOnServer(sharedCommand, context, activeEndpoint);
        rememberCredentials(sharedCommand, context, activeEndpoint);
        if (!sharedCommand.requiresAuthentication()) {
            return activeResult;
        }

        var otherEndpoints = context.sessions()
            .values()
            .stream()
            .filter(ServerSession::isAuthenticated)
            .map(ServerSession::endpoint)
            .filter(endpoint -> !endpoint.equals(activeEndpoint))
            .toList();
        if (otherEndpoints.isEmpty()) {
            return activeResult;
        }
        if (!confirmOtherServers(context)) {
            return activeResult;
        }

        StringJoiner result = new StringJoiner("\n");
        result.add("[" + activeEndpoint + "] " + activeResult);
        for (ServerEndpoint endpoint : otherEndpoints) {
            try {
                result.add(
                    "[" +
                    endpoint +
                    "] " +
                    executeOnServer(sharedCommand, context, endpoint)
                );
            } catch (Exception e) {
                result.add("[" + endpoint + "] failed: " + e.getMessage());
            }
        }
        return result.toString();
    }

    private String executeReadOnlyOnAllServers(
        SharedCommand sharedCommand,
        ClientContext context
    ) {
        StringJoiner result = new StringJoiner("\n");
        for (ServerEndpoint endpoint : context.sessions().keySet()) {
            try {
                result.add(
                    "[" +
                    endpoint +
                    "]\n" +
                    executeOnServer(sharedCommand, context, endpoint)
                );
            } catch (Exception e) {
                result.add("[" + endpoint + "] failed: " + e.getMessage());
            }
        }
        return result.toString();
    }

    private boolean confirmOtherServers(ClientContext context) {
        if (!promptForMultiServerMutations) {
            return false;
        }
        System.out.print("Execute this command on other connected servers? [y/N]: ");
        String answer = context.inputParser().getInputSource().readLine();
        return answer != null && answer.trim().equalsIgnoreCase("y");
    }

    private String executeOnServer(
        SharedCommand sharedCommand,
        ClientContext context,
        ServerEndpoint endpoint
    )
        throws Exception {
        IOException lastIOException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (
                Selector selector = Selector.open();
                SocketChannel socketChannel = SocketChannel.open()
            ) {
                socketChannel.configureBlocking(false);
                socketChannel.connect(
                    new InetSocketAddress(endpoint.host(), endpoint.port())
                );
                socketChannel.register(selector, SelectionKey.OP_CONNECT);

                ByteBuffer writeBuffer = TransportCodec.encode(
                    CommandRequest.of(
                        sharedCommand,
                        context.session(endpoint).login(),
                        context.session(endpoint).password()
                    )
                );
                ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                ByteBuffer payloadBuffer = null;

                while (true) {
                    if (selector.select(SELECT_TIMEOUT_MS) == 0) {
                        throw new IOException("Timed out while waiting for server response");
                    }
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (key.isConnectable()) {
                            if (channel.finishConnect()) {
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                        } else if (key.isWritable()) {
                            channel.write(writeBuffer);
                            if (!writeBuffer.hasRemaining()) {
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        } else if (key.isReadable()) {
                            if (payloadBuffer == null) {
                                int bytesRead = channel.read(lengthBuffer);
                                if (bytesRead == -1) {
                                    throw new IOException(
                                        "Server closed connection before sending response"
                                    );
                                }
                                if (lengthBuffer.hasRemaining()) {
                                    continue;
                                }
                                lengthBuffer.flip();
                                payloadBuffer = ByteBuffer.allocate(lengthBuffer.getInt());
                            }

                            int bytesRead = channel.read(payloadBuffer);
                            if (bytesRead == -1) {
                                throw new IOException(
                                    "Server closed connection before sending full response"
                                );
                            }
                            if (payloadBuffer.hasRemaining()) {
                                continue;
                            }
                            return unwrapResponse(
                                TransportCodec.decode(payloadBuffer.array()),
                                context,
                                endpoint
                            );
                        }
                    }
                }
            } catch (IOException e) {
                lastIOException = e;
            }
        }
        throw new IllegalStateException(
            "Server is temporarily unavailable. Please try again later.",
            lastIOException
        );
    }

    private String unwrapResponse(
        Object responseObject,
        ClientContext context,
        ServerEndpoint endpoint
    ) {
        if (!(responseObject instanceof CommandResponse response)) {
            throw new IllegalStateException("Unsupported response type");
        }
        if (!response.success()) {
            throw new IllegalStateException(response.message());
        }
        return response.message();
    }

    private void rememberCredentials(
        SharedCommand command,
        ClientContext context,
        ServerEndpoint endpoint
    ) {
        if (command instanceof LoginCommand loginCommand) {
            context.authenticate(
                endpoint,
                loginCommand.login(),
                loginCommand.password()
            );
        } else if (command instanceof RegisterCommand registerCommand) {
            context.authenticate(
                endpoint,
                registerCommand.login(),
                registerCommand.password()
            );
        }
    }
}
