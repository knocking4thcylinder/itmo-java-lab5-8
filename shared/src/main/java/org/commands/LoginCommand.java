package org.commands;

/**
 * Authenticates an existing user on the server.
 */
public class LoginCommand extends SharedCommand {

    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;

    public LoginCommand(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        this.login = login;
        this.password = password;
    }

    @Override
    public String exec(SharedCommandContext context) throws Exception {
        context.login(login, password);
        return "logged in as " + login;
    }

    public String login() {
        return login;
    }

    public String password() {
        return password;
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }
}
