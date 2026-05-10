package org.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hashes and verifies passwords for database storage.
 */
public class PasswordHasher {

    private static final int SALT_BYTES = 16;
    private static final String ALGORITHM = "SHA-512";
    private static final String SEPARATOR = ":";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates a salted password hash.
     *
     * @param password plain password
     * @return encoded salt and hash
     */
    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = digest(salt, password);
        return encode(salt) + SEPARATOR + encode(hash);
    }

    /**
     * Verifies a plain password against a stored salted hash.
     *
     * @param password plain password
     * @param storedHash stored salt and hash
     * @return true if password matches
     */
    public boolean verify(String password, String storedHash) {
        String[] parts = storedHash.split(SEPARATOR, 2);
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = digest(salt, password);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] digest(byte[] salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ALGORITHM + " is not available", e);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
