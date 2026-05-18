package org.commands;

/**
 * Client-side state for one connected server.
 */
public class ServerSession {

    private final ServerEndpoint endpoint;
    private String login;
    private String password;

    public ServerSession(ServerEndpoint endpoint) {
        this.endpoint = java.util.Objects.requireNonNull(
            endpoint,
            "Server endpoint cannot be null"
        );
    }

    public ServerEndpoint endpoint() {
        return endpoint;
    }

    public String login() {
        return login;
    }

    public String password() {
        return password;
    }

    public boolean isAuthenticated() {
        return login != null && password != null;
    }

    public void authenticate(String login, String password) {
        this.login = java.util.Objects.requireNonNull(
            login,
            "Login cannot be null"
        );
        this.password = java.util.Objects.requireNonNull(
            password,
            "Password cannot be null"
        );
    }

    public void clearAuthentication() {
        this.login = null;
        this.password = null;
    }
}
