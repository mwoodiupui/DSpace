/*
 * Copyright 2019 Mark H. Wood.
 */

package org.dspace.eperson;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Base for creating password hash classes.
 *
 * @author mhwood
 */
public abstract class AbstractPasswordHash {
    private static final Logger LOG = LogManager.getLogger();

    protected static final String ALGORITHM_PROPERTY = "authentication-password.digestAlgorithm";

    protected static final String DEFAULT_DIGEST_ALGORITHM = "SHA-512"; // XXX magic

    protected static final ConfigurationService CONFIG = DSpaceServicesFactory.getInstance().getConfigurationService();

    protected static final int RESEED_INTERVAL = 100;

    /**
     * A secure random number generator instance.
     */
    protected static SecureRandom rng = null;

    /**
     * How many times has the RNG been called without re-seeding?
     */
    protected static int rngUses;

    /** Random salt to confound rainbow tables. */
    protected byte[] salt;

    /** Digest algorithm. */
    protected String hashAlgorithm;

    /**
     * The digest algorithm used if none is configured.
     *
     * @return the value of hashAlgorithm
     */
    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    /**
     * The digest algorithm used if none is configured.
     *
     * @return name of the default digest.
     */
    public static String getDefaultAlgorithm() {
        return PasswordHash.DEFAULT_DIGEST_ALGORITHM;
    }

    /**
     * Get the hash.
     *
     * @return the value of hash
     */
    abstract byte[] getHash();

    /**
     * Get the hash, as a String.
     *
     * @return hash encoded as hexadecimal digits, or null if none.
     */
    abstract String getHashString();

    /**
     * Get the salt.
     *
     * @return the value of salt
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Get the salt, as a String.
     *
     * @return salt encoded as hexadecimal digits, or null if none.
     */
    public String getSaltString() {
        if (null != salt) {
            return new String(Hex.encodeHex(salt));
        } else {
            return null;
        }
    }

    /**
     * Is this the string whose hash I hold?
     *
     * @param secret string to be hashed and compared to this hash.
     * @return true if secret hashes to the value held by this instance.
     */
    abstract boolean matches(String secret);

    /**
     * Generate an array of random bytes.
     * @return
     */
    protected synchronized byte[] generateSalt() {
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
            rng.setSeed(rng.generateSeed(PasswordHash.SEED_BYTES));
            rngUses = 0;
        }
        salt = new byte[PasswordHash.SALT_BYTES];
        rng.nextBytes(salt);
        return salt;
    }
}
