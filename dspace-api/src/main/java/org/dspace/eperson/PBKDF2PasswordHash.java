/*
 * Copyright 2019 Mark H. Wood.
 */

package org.dspace.eperson;

import java.security.NoSuchAlgorithmException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The PBKDF2 hash of a password.
 *
 * @author mhwood
 */
public class PBKDF2PasswordHash
        extends AbstractPasswordHash {
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Don't allow empty instances.
     */
    private PBKDF2PasswordHash() {
    }

    /**
     * Construct a hash structure from existing data, just for passing around.
     *
     * @param hashAlgorithm the digest hashAlgorithm used in producing {@code hash}.
     *                  If empty, set to null.  Other methods will treat this as unsalted MD5.
     *                  If you want salted multi-round MD5, specify "MD5".
     * @param salt      the salt hashed with the secret, or null.
     * @param hash      the hashed secret.
     */
    public PBKDF2PasswordHash(String hashAlgorithm, byte[] salt, byte[] hash) {
    }

    /**
     * Convenience:  like {@link #PBKDF2PasswordHash(String, byte[], byte[])} but with
     * hexadecimal-encoded {@code String}s.
     *
     * @param hashAlgorithm the digest hashAlgorithm used in producing {@code hash}.
     *                  If empty, set to null.  Other methods will treat this as unsalted MD5.
     *                  If you want salted multi-round MD5, specify "MD5".
     * @param salt      hexadecimal digits encoding the bytes of the salt, or null.
     * @param hash      hexadecimal digits encoding the bytes of the hash.
     * @throws DecoderException if salt or hash is not proper hexadecimal.
     */
    public PBKDF2PasswordHash(String hashAlgorithm, String salt, String hash) {
    }

    /**
     * Construct a hash structure from a cleartext password using the configured
     * digest algorithm.
     *
     * @param password the secret to be hashed.
     */
    public PBKDF2PasswordHash(String password) {
        // Generate some salt
        salt = generateSalt();

        // What digest algorithm to use?
        hashAlgorithm = CONFIG.getPropertyAsType(ALGORITHM_PROPERTY, DEFAULT_DIGEST_ALGORITHM);

        // Hash it!
        try {
            hash = digest(salt, hashAlgorithm, password);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage());
            hash = new byte[] {0};
        }
    }

    @Override
    public String getHashAlgorithm() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getHash() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getHashString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean matches(String secret) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
