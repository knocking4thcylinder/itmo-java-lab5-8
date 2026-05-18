package org.commands;

import java.util.Objects;

/**
 * Network address of a server known to the client.
 *
 * @param host server host
 * @param port server port
 */
public record ServerEndpoint(String host, int port) {

    public ServerEndpoint {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Server host cannot be null or blank");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Server port must be in 1..65535");
        }
        host = host.trim();
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
