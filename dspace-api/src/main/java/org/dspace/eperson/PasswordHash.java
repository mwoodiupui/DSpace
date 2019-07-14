/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

/**
 *
 * @author mhwood
 */
public interface PasswordHash {
    /**
     * Digest a secret using the configured hashing algorithm and fresh salt.
     *
     * @param secret hash this.
     */
    public void hash(String secret);

    /**
     * Replace the hash.  Only use this when setting up for comparison.
     * @param hash
     */
    void setHash(byte[] hash);

    /**
     * Get the hash.
     *
     * @return the value of hash
     */
    byte[] getHash();

    /**
     * The hash algorithm used if none is configured.
     *
     * @return the value of hashAlgorithm
     */
    String getHashAlgorithm();

    /**
     * Get the hash, as a String.
     *
     * @return hash encoded as hexadecimal digits, or null if none.
     */
    String getHashString();

    /**
     * Replace the salt.  Only use this when setting up for comparison.
     *
     * @param salt new salt.
     */
    void setSalt(byte[] salt);

    /**
     * Get the salt.
     *
     * @return the value of salt
     */
    byte[] getSalt();

    /**
     * Get the salt, as a String.
     *
     * @return salt encoded as hexadecimal digits, or null if none.
     */
    String getSaltString();

    /**
     * Is this the string whose hash I hold?
     *
     * @param secret string to be hashed and compared to this hash.
     * @return true if secret hashes to the value held by this instance.
     */
    boolean equals(String secret);
}
