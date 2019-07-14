/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.eperson;

/**
 * Factory for SimplePasswordHash.
 *
 * @author mhwood
 */
public class SimplePasswordHashFactory
        implements PasswordHashFactory {
    @Override
    public PasswordHash createInstance(String hashAlgorithm) {
        return new SimplePasswordHash(hashAlgorithm);
    }

    @Override
    public PasswordHash createInstance(String hashAlgorithm, byte[] salt,
            byte[] hash) {
        return new SimplePasswordHash(hashAlgorithm, salt, hash);
    }

    /**
     * Cheap solution:  just list the Standard Algorithms.  These are known
     * by every standard-conforming implementation.  A better solution might be
     * found at https://stackoverflow.com/a/24983009/2916377 but I'm too lazy to
     * work it out now.
     */
    private static final String[] STANDARD_HASH_ALGORITHMS
            = { "MD2", "MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512" };

    @Override
    public boolean isKnownHashAlgorithm(String hashAlgorithm) {
        if (null == hashAlgorithm || hashAlgorithm.isEmpty()) {
            return true; // This class handles original unsalted 'crypt' hash.
        }

        for (String candidate : STANDARD_HASH_ALGORITHMS) {
            if (candidate.equals(hashAlgorithm)) {
                return true;
            }
        }

        return false;
    }
}
