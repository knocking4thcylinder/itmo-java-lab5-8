package org.commands;

/**
 * Adds a server to the client and makes it active.
 */
public class ConnectCommand extends ClientCommand {

    private final ServerEndpoint endpoint;

    public ConnectCommand(String host, int port) {
        this.endpoint = new ServerEndpoint(host, port);
    }

    @Override
    public String exec(ClientContext context) {
        context.connect(endpoint);
        return "connected to " + endpoint + " and made it active";
    }
}
