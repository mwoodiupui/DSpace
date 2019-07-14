/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.validation.constraints.NotNull;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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

    /** Only known hash algorithm for Java PBKDF2. */
    static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA1";

    /** Required length of hash in bits. */
    static final int KEY_LENGTH = 64 * 8; // MAGIC 64 bytes

    /**
     * Construct an unused hash structure for a given algorithm.
     *
     * @param hashAlgorithm ignored.
     */
    PBKDF2PasswordHash(String hashAlgorithm) {
        this.hashAlgorithm = HASH_ALGORITHM;
    }

    /**
     * Construct a hash structure from existing data, just for passing around.
     *
     * @param hashAlgorithm the hash hashAlgorithm used in producing {@code hash}.
     *                      Not actually used with PBKDF2.
     * @param salt      the salt hashed with the secret, or null.
     * @param hash      the hashed secret.
     */
    public PBKDF2PasswordHash(String hashAlgorithm, byte[] salt, byte[] hash) {
        if ((null != hashAlgorithm) && hashAlgorithm.isEmpty()) {
            this.hashAlgorithm = null;
        } else {
            this.hashAlgorithm = HASH_ALGORITHM;
        }
        this.salt = salt;
        this.hash = hash;
    }

    /**
     * Convenience:  like {@link #PBKDF2PasswordHash(String, byte[], byte[])}
     * but with hexadecimal-encoded {@code String}s.
     *
     * @param hashAlgorithm the hash hashAlgorithm used in producing {@code hash}.
     *                      Not actually used with PBKDF2.
     * @param salt      hexadecimal digits encoding the bytes of the salt, or null.
     * @param hash      hexadecimal digits encoding the bytes of the hash.
     * @throws DecoderException if salt or hash is not proper hexadecimal.
     */
    PBKDF2PasswordHash(String hashAlgorithm, @NotNull String salt,
            @NotNull String hash)
            throws DecoderException {
        this.hashAlgorithm = HASH_ALGORITHM;

        if (null == salt) {
            throw new IllegalArgumentException("salt is null");
        } else {
            this.salt = Hex.decodeHex(salt.toCharArray());
        }

        if (null == hash) {
            throw new IllegalArgumentException("hash is null");
        } else {
            this.hash = Hex.decodeHex(hash.toCharArray());
        }
    }

    public static String getDefaultAlgorithm() {
        return HASH_ALGORITHM;
    }

    @Override
    public void hash(String secret) {
        salt = generateSalt(SALT_BYTES);
        try {
            hash = digest(salt, null, secret);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.error("Digestion failed:  {}", e.getMessage());
            hash = new byte[] {0};
        }
    }

    /* Hash a secret using PBKDF2 with HMAC SHA1. */
    @Override
    protected byte[] digest(byte[] salt, String hashAlgorithm, String secret)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(secret.toCharArray(), salt,
                getHashRounds(), KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(this.hashAlgorithm);
        return skf.generateSecret(spec).getEncoded();
    }
}
