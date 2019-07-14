/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.eperson;

/**
 * Initialize a PBKDF2 password hash hash.
 *
 * @author mhwood
 */
public class PBKDF2PasswordHashFactory
        implements PasswordHashFactory {
    @Override
    public PasswordHash createInstance(String hashAlgorithm) {
        return new PBKDF2PasswordHash(hashAlgorithm);
    }

    @Override
    public PasswordHash createInstance(String hashAlgorithm, byte[] hash,
            byte[] salt) {
        return new PBKDF2PasswordHash(hashAlgorithm, salt, hash);
    }

    @Override
    public boolean isKnownHashAlgorithm(String hashAlgorithm) {
        return PBKDF2PasswordHash.HASH_ALGORITHM.equals(hashAlgorithm);
    }
}
