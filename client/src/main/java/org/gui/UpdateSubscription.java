package org.gui;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.commands.ServerEndpoint;
import org.commands.SubscribeUpdatesCommand;
import org.shared.CommandRequest;
import org.shared.CommandResponse;
import org.shared.TransportCodec;

/**
 * Maintains a persistent server-to-client update notification connection.
 */
final class UpdateSubscription implements AutoCloseable {

    private final ServerEndpoint endpoint;
    private final String login;
    private final String password;
    private final Runnable onUpdate;
    private final Consumer<String> onError;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile boolean running;
    private volatile SocketChannel channel;

    UpdateSubscription(
        ServerEndpoint endpoint,
        String login,
        String password,
        Runnable onUpdate,
        Consumer<String> onError
    ) {
        this.endpoint = java.util.Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        this.login = java.util.Objects.requireNonNull(login, "Login cannot be null");
        this.password = java.util.Objects.requireNonNull(password, "Password cannot be null");
        this.onUpdate = java.util.Objects.requireNonNull(onUpdate, "Update callback cannot be null");
        this.onError = java.util.Objects.requireNonNull(onError, "Error callback cannot be null");
    }

    void start() {
        running = true;
        worker.submit(this::listenWithReconnect);
    }

    private void listenWithReconnect() {
        while (running) {
            try {
                listenOnce();
            } catch (Exception e) {
                closeChannel();
                if (running) {
                    javax.swing.SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
                }
                sleepBeforeReconnect();
            }
        }
    }

    private void listenOnce() throws Exception {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            channel = socketChannel;
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(endpoint.host(), endpoint.port()));
            writeRequest(socketChannel);
            while (running) {
                Object object = TransportCodec.decode(readPayload(socketChannel));
                if (object instanceof CommandResponse response &&
                    response.success() &&
                    "collection-updated".equals(response.message())) {
                    javax.swing.SwingUtilities.invokeLater(onUpdate);
                }
            }
        } finally {
            channel = null;
        }
    }

    private void writeRequest(SocketChannel socketChannel) throws IOException {
        ByteBuffer request = TransportCodec.encode(
            CommandRequest.of(new SubscribeUpdatesCommand(), login, password)
        );
        while (request.hasRemaining()) {
            socketChannel.write(request);
        }
    }

    private byte[] readPayload(SocketChannel socketChannel) throws IOException {
        DataInputStream inputStream = new DataInputStream(
            Channels.newInputStream(socketChannel)
        );
        int payloadLength = inputStream.readInt();
        byte[] payload = new byte[payloadLength];
        inputStream.readFully(payload);
        return payload;
    }

    private void sleepBeforeReconnect() {
        try {
            Thread.sleep(1_500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        running = false;
        closeChannel();
        worker.shutdownNow();
    }

    private void closeChannel() {
        SocketChannel currentChannel = channel;
        if (currentChannel == null) {
            return;
        }
        try {
            currentChannel.close();
        } catch (IOException ignored) {
            // Closing a stale subscription is best-effort.
        }
    }
}
