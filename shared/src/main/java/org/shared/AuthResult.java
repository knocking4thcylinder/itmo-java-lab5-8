package org.shared;

import java.io.Serializable;
import java.util.Objects;

/**
 * Authentication data returned by the server after login or registration.
 *
 * @param login authenticated login
 * @param authToken session token
 */
public record AuthResult(
    String login,
    String authToken
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public AuthResult {
        Objects.requireNonNull(login, "Login cannot be null");
        Objects.requireNonNull(authToken, "Auth token cannot be null");
    }
}
