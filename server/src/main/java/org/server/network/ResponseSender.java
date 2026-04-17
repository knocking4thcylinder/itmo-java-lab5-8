package org.server.network;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.protocol.CommandResponse;
import org.protocol.SerializationUtils;

public final class ResponseSender {

    public void write(Socket socket, CommandResponse response)
        throws IOException {
        DataOutputStream outputStream = new DataOutputStream(
            new BufferedOutputStream(socket.getOutputStream())
        );
        byte[] payload = SerializationUtils.serialize(response);
        outputStream.writeInt(payload.length);
        outputStream.write(payload);
        outputStream.flush();
    }
}
