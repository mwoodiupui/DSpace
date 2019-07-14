/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.eperson;

import javax.inject.Inject;

import org.dspace.eperson.service.PasswordHashService;

/**
 * Delegates to the appropriate hash implementation.
 *
 * @author mhwood
 */
public class PasswordHashServiceImpl implements PasswordHashService {
    /** All available PasswordHash implementations. */
    @Inject
    private PasswordHashFactory[] hashes;

    /** Default hash if not specified. */
    @Inject
    private PasswordHashFactory defaultHash;

    /** Only the DI framework should instantiate this. */
    private PasswordHashServiceImpl() { }

    /**
     * Initialize
     * @param hashAlgorithm the desired hash algorithm.  {@code null} means the
     *                      default algorithm.
     * @return an implementation which can use the given hash algorithm.
     * @throws IllegalArgumentException if no implementation claims the algorithm.
     */
    @Override
    public PasswordHash getPasswordHashInstance(String hashAlgorithm) {
        if (null == hashAlgorithm) {
            return defaultHash.createInstance(hashAlgorithm);
        }
        for (PasswordHashFactory passwordHashFactory : hashes) {
            if (passwordHashFactory.isKnownHashAlgorithm(hashAlgorithm)) {
                return passwordHashFactory.createInstance(hashAlgorithm);
            }
        }

        throw new IllegalArgumentException("Unknown hash " + hashAlgorithm);
    }

    @Override
    public PasswordHash getPasswordHashInstance(String hashAlgorithm, byte[] salt, byte[] hash) {
        if (null == hashAlgorithm) {
            throw new IllegalArgumentException("algorithm cannot be null");
        }
        for (PasswordHashFactory passwordHashFactory : hashes) {
            if (passwordHashFactory.isKnownHashAlgorithm(hashAlgorithm)) {
                return passwordHashFactory.createInstance(hashAlgorithm, hash,
                        salt);
            }
        }
        throw new IllegalArgumentException ("Unknown hash " + hashAlgorithm);
    }

    /**
     * @param hashes the hashes to set
     */
    public void setHashes(PasswordHashFactory[] hashes) {
        this.hashes = hashes;
    }

    /**
     * @param defaultHash the defaultHash to set
     */
    public void setDefaultHash(PasswordHashFactory defaultHash) {
        this.defaultHash = defaultHash;
    }
}
