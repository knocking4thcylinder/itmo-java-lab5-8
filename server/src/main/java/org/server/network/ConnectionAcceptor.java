package org.server.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;

public final class ConnectionAcceptor implements Closeable {

    private final ServerSocket serverSocket;

    public ConnectionAcceptor(int port, int timeoutMillis) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setSoTimeout(timeoutMillis);
    }

    public Optional<Socket> accept() throws IOException {
        try {
            return Optional.of(serverSocket.accept());
        } catch (SocketTimeoutException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
