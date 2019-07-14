/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.eperson;

import static org.dspace.eperson.AbstractPasswordHash.SALT_BYTES;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * For handling digested secrets (such as passwords).
 * Use {@link #PasswordHash(String, byte[], byte[])} to package and manipulate
 * secrets that have already been hashed, and {@link #PasswordHash(String)} for
 * plaintext secrets.  Compare a plaintext candidate to a hashed secret with
 * {@link #equals(String)}.
 *
 * @author mwood
 */
public class SimplePasswordHash
        extends AbstractPasswordHash {
    private static final Logger LOG = LogManager.getLogger();
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * Don't allow empty instances.
     */
    private SimplePasswordHash() {
    }

    /**
     * Construct a hash structure from existing data, just for passing around.
     *
     * @param algorithm the hash hashAlgorithm used in producing {@code hash}.
     *                  If empty, set to null.  Other methods will treat this as unsalted MD5.
     *                  If you want salted multi-round MD5, specify "MD5".
     * @param salt      the salt hashed with the secret, or null.
     * @param hash      the hashed secret.
     */
    public SimplePasswordHash(String algorithm, byte[] salt, byte[] hash) {
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
     * @param algorithm the hash hashAlgorithm used in producing {@code hash}.
     *                  If empty, set to null.  Other methods will treat this as unsalted MD5.
     *                  If you want salted multi-round MD5, specify "MD5".
     * @param salt      hexadecimal digits encoding the bytes of the salt, or null.
     * @param hash      hexadecimal digits encoding the bytes of the hash.
     * @throws DecoderException if salt or hash is not proper hexadecimal.
     */
    public SimplePasswordHash(String algorithm, String salt, String hash)
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
     * hash hashAlgorithm.
     *
     * @param password the secret to be hashed.
     */
    public SimplePasswordHash(String password) {
        // Generate some salt
        salt = generateSalt(SALT_BYTES);

        // What hash hashAlgorithm to use?
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
    public void hash(String secret) {
        salt = generateSalt(SALT_BYTES);
        try {
            hash = digest(salt, null, secret);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Digestion failed:  {}", e.getMessage());
            hash = new byte[] {0};
        }
    }

    @Override
    protected byte[] digest(byte[] salt, String algorithm, String secret)
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

        // Set up a hash
        digester = MessageDigest.getInstance(algorithm);

        // Grind up the salt with the password, yielding a hash
        if (null != salt) {
            digester.update(salt);
        }

        digester.update(secret.getBytes(UTF_8)); // Round 0

        int nRounds = getHashRounds();
        for (int round = 1; round < nRounds; round++) {
            byte[] lastRound = digester.digest();
            digester.reset();
            digester.update(lastRound);
        }

        return digester.digest();
    }
}
