package org.auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores short-lived server-side login sessions.
 */
public class SessionManager {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new session for a login.
     *
     * @param login authenticated login
     * @return generated session token
     */
    public String createSession(String login) {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(tokenBytes);
        sessions.put(token, login);
        return token;
    }

    /**
     * Finds login associated with a token.
     *
     * @param token auth token
     * @return login if token exists
     */
    public Optional<String> findLogin(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }
}
