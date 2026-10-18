package com.erp.platform;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Password hashing with PBKDF2-HMAC-SHA256 (NFR-S-02).
 *
 * <p>PBKDF2 is used rather than Argon2id because it ships with the JDK, so the
 * desktop build carries no extra native dependency. The parameters follow the
 * OWASP Password Storage Cheat Sheet: a 16-byte random salt, 256-bit derived key
 * and a work factor that is stored with the hash so it can be raised later
 * without invalidating existing passwords.
 *
 * <p>Stored format: {@code iterations:saltBase64:hashBase64}.
 */
@ApplicationScoped
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    public String hash(String plain) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] key = derive(plain.toCharArray(), salt, ITERATIONS, KEY_BITS);
        return ITERATIONS + ":" + base64(salt) + ":" + base64(key);
    }

    public boolean verify(String plain, String stored) {
        if (plain == null || stored == null || stored.indexOf(':') < 0) {
            return false;
        }
        String[] parts = stored.split(":", 3);
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = derive(plain.toCharArray(), salt, iterations, expected.length * 8);
            // Constant-time comparison so the response cannot be timed to leak the hash.
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations, int keyBits) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is unavailable in this JVM.", e);
        }
    }

    private static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
