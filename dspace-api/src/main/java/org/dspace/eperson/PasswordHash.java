/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.eperson;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For handling digested secrets (such as passwords).
 * Use {@link #PasswordHash(String, byte[], byte[])} to package and manipulate
 * secrets that have already been hashed, and {@link #PasswordHash(String)} for
 * plaintext secrets.  Compare a plaintext candidate to a hashed secret with
 * {@link #matches(String)}.
 *
 * @author mwood
 */
public class PasswordHash
        extends AbstractPasswordHash {
    private static final Logger log = LoggerFactory.getLogger(PasswordHash.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final int SALT_BYTES = 128 / 8; // XXX magic we want 128 bits
    private static final int HASH_ROUNDS = 1024; // XXX magic 1024 rounds
    private static final int SEED_BYTES = 64; // XXX magic
    private byte[] hash;

    /**
     * Don't allow empty instances.
     */
    private PasswordHash() {
    }

    /**
     * Construct a hash structure from existing data, just for passing around.
     *
     * @param algorithm the digest hashAlgorithm used in producing {@code hash}.
     *                  If empty, set to null.  Other methods will treat this as unsalted MD5.
     *                  If you want salted multi-round MD5, specify "MD5".
     * @param salt      the salt hashed with the secret, or null.
     * @param hash      the hashed secret.
     */
    public PasswordHash(String algorithm, byte[] salt, byte[] hash) {
        if ((null != algorithm) && algorithm.isEmpty()) {
            this.hashAlgorithm = null;
        } else {
            this.hashAlgorithm = algorithm;
        }

        this.salt = salt;

        this.hash = hash;
    }

    /**
     * Convenience:  like {@link #PasswordHash(String, byte[], byte[])} but with
     * hexadecimal-encoded {@code String}s.
     *
     * @param algorithm the digest hashAlgorithm used in producing {@code hash}.
     *                  If empty, set to null.  Other methods will treat this as unsalted MD5.
     *                  If you want salted multi-round MD5, specify "MD5".
     * @param salt      hexadecimal digits encoding the bytes of the salt, or null.
     * @param hash      hexadecimal digits encoding the bytes of the hash.
     * @throws DecoderException if salt or hash is not proper hexadecimal.
     */
    public PasswordHash(String algorithm, String salt, String hash)
        throws DecoderException {
        if ((null != algorithm) && algorithm.isEmpty()) {
            this.hashAlgorithm = null;
        } else {
            this.hashAlgorithm = algorithm;
        }

        if (null == salt) {
            this.salt = null;
        } else {
            this.salt = Hex.decodeHex(salt.toCharArray());
        }

        if (null == hash) {
            this.hash = null;
        } else {
            this.hash = Hex.decodeHex(hash.toCharArray());
        }
    }

    /**
     * Construct a hash structure from a cleartext password using the configured
 digest hashAlgorithm.
     *
     * @param password the secret to be hashed.
     */
    public PasswordHash(String password) {
        // Generate some salt
        salt = generateSalt();

        // What digest hashAlgorithm to use?
        hashAlgorithm = CONFIG.getPropertyAsType(ALGORITHM_PROPERTY, DEFAULT_DIGEST_ALGORITHM);

        // Hash it!
        try {
            hash = digest(salt, hashAlgorithm, password);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            hash = new byte[] {0};
        }
    }

    /**
     * Is this the string whose hash I hold?
     *
     * @param secret string to be hashed and compared to this hash.
     * @return true if secret hashes to the value held by this instance.
     */
    @Override
    public boolean matches(String secret) {
        byte[] candidate;
        try {
            candidate = digest(salt, hashAlgorithm, secret);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            return false;
        }
        return Arrays.equals(candidate, hash);
    }

    /**
     * Get the hash.
     *
     * @return the value of hash
     */
    @Override
    public byte[] getHash() {
        return hash;
    }

    /**
     * Get the hash, as a String.
     *
     * @return hash encoded as hexadecimal digits, or null if none.
     */
    @Override
    public String getHashString() {
        if (null != hash) {
            return new String(Hex.encodeHex(hash));
        } else {
            return null;
        }
    }

    /**
     * Generate a salted hash of a string using a given hashAlgorithm.
     *
     * @param salt      random bytes to salt the hash.
     * @param algorithm name of the digest hashAlgorithm to use.  Assume unsalted MD5 if null.
     * @param secret    the string to be hashed.  Null is treated as an empty string ("").
     * @return hash bytes.
     * @throws NoSuchAlgorithmException if hashAlgorithm is unknown.
     */
    private byte[] digest(byte[] salt, String algorithm, String secret)
        throws NoSuchAlgorithmException {
        MessageDigest digester;

        if (null == secret) {
            secret = "";
        }

        // Special case:  old unsalted one-trip MD5 hash.
        if (null == algorithm) {
            digester = MessageDigest.getInstance("MD5");
            digester.update(secret.getBytes(UTF_8));
            return digester.digest();
        }

        // Set up a digest
        digester = MessageDigest.getInstance(algorithm);

        // Grind up the salt with the password, yielding a hash
        if (null != salt) {
            digester.update(salt);
        }

        digester.update(secret.getBytes(UTF_8)); // Round 0

        for (int round = 1; round < HASH_ROUNDS; round++) {
            byte[] lastRound = digester.digest();
            digester.reset();
            digester.update(lastRound);
        }

        return digester.digest();
    }
}
