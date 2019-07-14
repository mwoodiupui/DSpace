/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.utils.DSpace;

/**
 * Base for creating password hash classes.
 *
 * @author mhwood
 */
public abstract class AbstractPasswordHash
        implements PasswordHash {
    private static final Logger LOG = LogManager.getLogger();

    private static final ConfigurationService configurationService
            = new DSpace().getConfigurationService();

    protected static final String ALGORITHM_PROPERTY
            = "authentication-password.digestAlgorithm";

    protected static final String DEFAULT_DIGEST_ALGORITHM = "SHA-512"; // XXX magic

    private static final int DEFAULT_HASH_ROUNDS = 1024;

    protected static final ConfigurationService CONFIG
            = DSpaceServicesFactory.getInstance().getConfigurationService();

    /** Re-seed after computing this many hashes. */
    protected static final int RESEED_INTERVAL = 100; // XXX magic

    /** How much randomness for re-seeding? */
    protected static final int SEED_BYTES = 64; // XXX magic

    /** How many bytes of salt to generate? */
    protected static final int SALT_BYTES = 128 / 8; // XXX magic we want 128 bytes

    /** How many times do we hash the secret? */
    private static int hashRounds;

    /** A secure random number generator instance. */
    protected static SecureRandom rng = null;

    /** How many times has the RNG been called without re-seeding? */
    protected static int rngUses;

    /** Random salt to confound rainbow tables. */
    protected byte[] salt;

    /** Digest algorithm. */
    protected String hashAlgorithm;

    /** Computed hash. */
    protected byte[] hash;

    /**
     * The hash algorithm used if none is configured.
     *
     * @return the value of hashAlgorithm
     */
    @Override
    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    /**
     * The hash algorithm used if none is configured.
     *
     * @return name of the default hash.
     */
    public static String getDefaultAlgorithm() {
        return DEFAULT_DIGEST_ALGORITHM;
    }

    @Override
    public void setHash(byte[] hash) {
        this.hash = hash;
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

    @Override
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    /**
     * Get the salt.
     *
     * @return the value of salt
     */
    @Override
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Get the salt, as a String.
     *
     * @return salt encoded as hexadecimal digits, or null if none.
     */
    @Override
    public String getSaltString() {
        if (null != salt) {
            return new String(Hex.encodeHex(salt));
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(String secret) {
        byte[] candidate;
        try {
            candidate = digest(salt, hashAlgorithm, secret);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.error("Failed to digest candidate password:  {}", e.getMessage());
            return false;
        }
        return Arrays.equals(candidate, hash);
    }

    /**
     * How many times to apply the hash.
     *
     * @return number of hashing rounds to apply to each secret.
     */
    int getHashRounds() {
        if (hashRounds <= 0) {
            hashRounds = configurationService
                    .getIntProperty("eperson.hash.rounds", DEFAULT_HASH_ROUNDS);
        }
        return hashRounds;
    }

    /**
     * Generate a salted hash of a string using a given hashAlgorithm.
     *
     * @param salt      random bytes to salt the hash.
     * @param hashAlgorithm name of the hash hashAlgorithm to use.  Assume unsalted MD5 if null.
     * @param secret    the string to be hashed.  Null is treated as an empty string ("").
     * @return hash bytes.
     * @throws NoSuchAlgorithmException passed through.
     * @throws InvalidKeySpecException passed through.
     */
    protected abstract byte[] digest(byte[] salt, String hashAlgorithm, String secret)
            throws NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * Generate an array of random bytes.
     * @param saltBytes generate this many random bytes.
     * @return
     */
    protected synchronized byte[] generateSalt(int saltBytes) {
        // Initialize a random-number generator
        if (null == rng) {
            rng = new SecureRandom();
            LOG.info("Initialized a random number stream using {} provided by {}",
                    rng.getAlgorithm(),
                    rng.getProvider());
            rngUses = 0;
        }
        if (rngUses++ > RESEED_INTERVAL) {
            // re-seed the generator periodically to break up possible patterns
            LOG.debug("Re-seeding the RNG");
            rng.setSeed(rng.generateSeed(SEED_BYTES));
            rngUses = 0;
        }
        salt = new byte[saltBytes];
        rng.nextBytes(salt);
        return salt;
    }
}
