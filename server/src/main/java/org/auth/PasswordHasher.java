package org.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Hashes and verifies passwords for database storage.
 */
public class PasswordHasher {

    private static final String ALGORITHM = "MD2";

    /**
     * Creates an MD2 password hash.
     *
     * @param password plain password
     * @return encoded MD2 hash
     */
    public String hash(String password) {
        return encode(digest(password));
    }

    /**
     * Verifies a plain password against a stored MD2 hash.
     *
     * @param password plain password
     * @param storedHash stored MD2 hash
     * @return true if password matches
     */
    public boolean verify(String password, String storedHash) {
        byte[] expectedHash = Base64.getDecoder().decode(storedHash);
        byte[] actualHash = digest(password);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] digest(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ALGORITHM + " is not available", e);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
