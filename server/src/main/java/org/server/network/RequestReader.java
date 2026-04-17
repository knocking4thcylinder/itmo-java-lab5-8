package org.server.network;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import org.protocol.CommandRequest;
import org.protocol.SerializationUtils;

public final class RequestReader {

    public CommandRequest read(Socket socket)
        throws IOException, ClassNotFoundException {
        DataInputStream inputStream = new DataInputStream(
            new BufferedInputStream(socket.getInputStream())
        );
        int payloadLength = inputStream.readInt();
        byte[] payload = inputStream.readNBytes(payloadLength);
        if (payload.length != payloadLength) {
            throw new EOFException("Unexpected end of request payload");
        }
        return (CommandRequest) SerializationUtils.deserialize(payload);
    }
}
