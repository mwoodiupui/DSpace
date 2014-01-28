/*
 * Copyright 2014 Indiana University.  All rights reserved.
 *
 * Mark H. Wood, IUPUI University Library, Jan 27, 2014
 */

package org.dspace.content;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.dspace.identifier.IdentifierProvider;
import org.dspace.identifier.IdentifierServiceCallback;

/**
 * Inform IdentifierService whether we want this kind of identifiers.
 *
 * @author mwood
 */
public class InstallItemIdentifierServiceCallback
        implements IdentifierServiceCallback
{
    /**
     * Map collectionIDs to sets of IdentifierProvider.
     * Configure with Spring or some such.
     * TODO: replace this ugliness with Collection metadata or properties.
     */
    @Inject
    Map<Integer, Set<Class>> wantedIdentifierClassesMap;

    /** IdentifierProviders for this collection. */
    private final Set<Class> wantedIdentifierClasses;

    /** Do not use */
    private InstallItemIdentifierServiceCallback() { wantedIdentifierClasses = null; }

    /**
     * Initialize a callback for a specific Collection.
     *
     * @param collection
     */
    public InstallItemIdentifierServiceCallback(int collection)
    {
        wantedIdentifierClasses = wantedIdentifierClassesMap.get(collection);
    }

    @Override
    public boolean registerP(Class<?extends IdentifierProvider> clazz)
    {
        return (null == wantedIdentifierClasses)
                || wantedIdentifierClasses.contains(clazz);
    }
}
