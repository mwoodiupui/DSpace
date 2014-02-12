/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.dspace.identifier.IdentifierProvider;
import org.dspace.identifier.IdentifierServiceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inform IdentifierService whether we want this kind of identifiers.
 * Collections are configured by mapping from collection_id to a Set of provider
 * Classes.  collection_id -1 can be used to map a default provider set, if the
 * default should be less than "all providers".  The mapping is injected using
 * {@link #setWantedIdentifierClassesMap(Map)}.
 *
 * @author mwood
 */
public class InstallItemIdentifierServiceCallback
        implements IdentifierServiceCallback
{
    /** Logging category */
    private static final Logger log
            = LoggerFactory.getLogger(InstallItemIdentifierServiceCallback.class);

    /**
     * Map collectionIDs to sets of IdentifierProvider.
     * Configure with Spring or some such.
     * TODO: replace this ugliness with Collection metadata or properties.
     */
    private static Map<Integer, Set<Class>> wantedIdentifierClassesMap;

    /** IdentifierProviders for this Collection. */
    private final Set<Class> wantedIdentifierClasses;

    /** IdentifierProviders for unconfigured Collections. */
    private static Set<Class> defaultIdentifierClasses;

    /** Do not use */
    public InstallItemIdentifierServiceCallback()
    {
        log.debug("Default constructor called.");
        wantedIdentifierClasses = defaultIdentifierClasses = null;
    }

    /**
     * Initialize a callback for a specific Collection.
     * NOTE:  the configuration should be injected before calling this constructor.
     *
     * @param collection the instance will represent this Collection.
     */
    public InstallItemIdentifierServiceCallback(int collection)
    {
        log.debug("Create callback for Collection {}", collection);
        wantedIdentifierClasses = wantedIdentifierClassesMap.get(collection);
    }

    /** Set the map between Collection and IdentifierProvider sets. */
    @Inject
    public void setWantedIdentifierClassesMap(Map<Integer, Set<Class>>map)
    {
        // XXX This is perhaps abusive.  We configure the class by
        // having Spring instantiate it and call the setter, stash the
        // configuration in a static reference, and then never use
        // that instance.
        // FIXME this should be done with Collection metadata when we have it.
        wantedIdentifierClassesMap = map;
        log.debug("set map {}", wantedIdentifierClassesMap);

        defaultIdentifierClasses = wantedIdentifierClassesMap.get(-1);
        log.debug("default map = {}", defaultIdentifierClasses);
    }

    /**
     * Test whether this Collection wants this provider.
     *
     * <p>Return true if neither this Collection nor the default list is configured.</p>
     *
     * <p>If this Collection is configured, return whether this provider is in this
     * Collection's list.</p>
     *
     * <p>If this Collection is not configured but there is a default list, return
     * whether this provider is in the default list.</p>
     *
     * <p>Thus, if you don't configure anything, all providers are always called.
     * If you want fewer providers called for some Collections, configure those
     * Collections' lists.  If you want some providers excluded by default,
     * configure the default list and any Collections that want something different.</p>
     *
     * @param clazz the provider under consideration.
     * @return whether this provider should be called.
     */
    @Override
    public boolean registerP(Class<?extends IdentifierProvider> clazz)
    {
        log.debug("Provider:  {}", clazz.getName());
        if (null == wantedIdentifierClasses) // This Collection not configured
        {
            log.debug("No configuration.");
            if (null == defaultIdentifierClasses) // Default not configured either
            {
                log.debug("No default:  Result true.");
                return true; // Default default:  all providers wanted
            }
            else
            {
                boolean wanted = defaultIdentifierClasses.contains(clazz); // Wanted by default?
                log.debug("Applying default {}:  result {}.", defaultIdentifierClasses, wanted);
                return wanted;
            }
        }
        else
        {
            boolean wanted = wantedIdentifierClasses.contains(clazz); // Wanted by this Collection?
            log.debug("Applying specific {}:  result {}", wantedIdentifierClasses, wanted);
            return wanted;
        }
    }
}
