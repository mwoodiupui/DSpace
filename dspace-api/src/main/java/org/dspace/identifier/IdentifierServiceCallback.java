/*
 * Copyright 2014 Indiana University.  All rights reserved.
 *
 * Mark H. Wood, IUPUI University Library, Jan 27, 2014
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
