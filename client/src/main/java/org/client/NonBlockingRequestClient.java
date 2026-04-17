package org.client;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.protocol.CommandRequest;
import org.protocol.CommandResponse;
import org.protocol.SerializationUtils;

public final class NonBlockingRequestClient {

    private static final int RETRY_COUNT = 5;
    private static final long RETRY_DELAY_MILLIS = 400L;

    public CommandResponse send(String host, int port, CommandRequest request)
        throws Exception {
        byte[] payload = SerializationUtils.serialize(request);

        for (int attempt = 1; attempt <= RETRY_COUNT; attempt++) {
            try {
                return exchange(host, port, payload);
            } catch (IOException exception) {
                if (attempt == RETRY_COUNT) {
                    throw new IOException(
                        "Server is temporarily unavailable",
                        exception
                    );
                }
                sleep(RETRY_DELAY_MILLIS);
            }
        }

        throw new IllegalStateException("Unreachable state");
    }

    private CommandResponse exchange(String host, int port, byte[] payload)
        throws Exception {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            while (!channel.finishConnect()) {
                sleep(25L);
            }

            ByteBuffer writeBuffer = ByteBuffer.allocate(
                Integer.BYTES + payload.length
            );
            writeBuffer.putInt(payload.length);
            writeBuffer.put(payload);
            writeBuffer.flip();
            writeFully(channel, writeBuffer);

            ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
            readFully(channel, lengthBuffer);
            lengthBuffer.flip();
            int responseLength = lengthBuffer.getInt();

            ByteBuffer responseBuffer = ByteBuffer.allocate(responseLength);
            readFully(channel, responseBuffer);
            responseBuffer.flip();

            byte[] responsePayload = new byte[responseLength];
            responseBuffer.get(responsePayload);
            return (CommandResponse) SerializationUtils.deserialize(
                responsePayload
            );
        }
    }

    private void writeFully(SocketChannel channel, ByteBuffer buffer)
        throws Exception {
        while (buffer.hasRemaining()) {
            int written = channel.write(buffer);
            if (written == 0) {
                sleep(10L);
            }
        }
    }

    private void readFully(SocketChannel channel, ByteBuffer buffer)
        throws Exception {
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read == -1) {
                throw new EOFException("Connection closed by server");
            }
            if (read == 0) {
                sleep(10L);
            }
        }
    }

    private void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
