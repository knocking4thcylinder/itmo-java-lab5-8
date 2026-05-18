package org.commands;

/**
 * Selects the active server.
 */
public class UseServerCommand extends ClientCommand {

    private final ServerEndpoint endpoint;

    public UseServerCommand(String host, int port) {
        this.endpoint = new ServerEndpoint(host, port);
    }

    @Override
    public String exec(ClientContext context) {
        context.useServer(endpoint);
        return "active server is now " + endpoint;
    }
}
