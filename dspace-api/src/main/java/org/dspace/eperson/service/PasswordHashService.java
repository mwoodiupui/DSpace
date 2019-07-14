/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.eperson.service;

import org.dspace.eperson.PasswordHash;

/**
 * Manage instances of PasswordHash.
 *
 * @author mhwood
 */
public interface PasswordHashService {
    /**
     * Initialize a PasswordHash with a given hashing algorithm, to compute new hashes.
     *
     * @param hashAlgorithm the desired hash algorithm.  {@code null} means the
     *                      default algorithm.
     * @return an implementation which can use the given hash algorithm.
     * @throws IllegalArgumentException if no implementation claims the algorithm.
     */
    PasswordHash getPasswordHashInstance(String hashAlgorithm);

    /**
     * Initialize a PasswordHash with algorithm, precomputed hash, precomputed salt.
     *
     * @param hashAlgorithm the hash algorithm used to compute {@link hash}.
     * @param salt the salt used when hashing the secret.
     * @param hash the hash of some secret.
     * @return an instance with all fields set.
     */
    PasswordHash getPasswordHashInstance(String hashAlgorithm, byte[] salt, byte[] hash);
}
