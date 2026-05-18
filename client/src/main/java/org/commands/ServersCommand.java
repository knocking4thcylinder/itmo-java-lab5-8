package org.commands;

import java.util.stream.Collectors;

/**
 * Lists servers known to the client.
 */
public class ServersCommand extends ClientCommand {

    @Override
    public String exec(ClientContext context) {
        ServerEndpoint activeEndpoint = context.activeEndpoint();
        return context.sessions()
            .values()
            .stream()
            .map(session -> formatSession(session, activeEndpoint))
            .collect(Collectors.joining("\n"));
    }

    private String formatSession(
        ServerSession session,
        ServerEndpoint activeEndpoint
    ) {
        String marker = session.endpoint().equals(activeEndpoint) ? "* " : "  ";
        String auth = session.isAuthenticated()
            ? " login=" + session.login()
            : " not logged in";
        return marker + session.endpoint() + auth;
    }
}
