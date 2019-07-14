/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

/**
 * Factory for PasswordHash instances.  Implement this for each subtype of
 * PasswordHash.  {@link PasswordHashService} will choose the implementation to
 * return.
 *
 * @author mhwood
 */
public interface PasswordHashFactory {
    /**
     * Instantiate a PasswordHash to be computed later, using a given hash algorithm.
     *
     * @param hashAlgorithm the required hash algorithm.
     * @return a new instance ready to hash secrets.
     */
    public PasswordHash createInstance(String hashAlgorithm);

    /**
     * Instantiate a PasswordHash with given hash algorithm, hash value and salt.
     * Used to create a hash "constant" for comparison with user input.
     *
     * @param hashAlgorithm the algorithm that was used to compute {@link hash}.
     * @param hash the hash of some secret.
     * @param salt the salt that was digested with the secret to compute the hash.
     * @return a new instance with all fields filled.
     */
    public PasswordHash createInstance(String hashAlgorithm, byte[] hash, byte[] salt);

    /**
     * Does this implementation understand a given hashing algorithm?
     *
     * @param hashAlgorithm the name of the required algorithm.
     * @return true if this implementation can use that algorithm.
     */
    boolean isKnownHashAlgorithm(String hashAlgorithm);
}
