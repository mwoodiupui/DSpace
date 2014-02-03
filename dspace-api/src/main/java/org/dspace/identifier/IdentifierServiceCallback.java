/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.identifier;

/**
 * Methods to allow IdentifierService clients to pick and choose acceptable providers.
 *
 * @author mwood
 */
public interface IdentifierServiceCallback
{
    /**
     * Is this provider wanted for registration?
     *
     * @param clazz the provider Class to be considered.
     * @return true if this provider should be called.
     */
    public boolean registerP(Class<?extends IdentifierProvider> clazz);
}
