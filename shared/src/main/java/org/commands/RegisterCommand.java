package org.commands;

/**
 * Registers a new user on the server.
 */
public class RegisterCommand extends SharedCommand {

    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;

    public RegisterCommand(String login, String password) {
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
        context.register(login, password);
        return "registered and logged in as " + login;
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }
}
