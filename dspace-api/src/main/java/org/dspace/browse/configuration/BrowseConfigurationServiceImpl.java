package org.dspace.browse.configuration;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Holder for the configuration of the browse functions.
 *
 * @author mwood
 */
@Named
public class BrowseConfigurationServiceImpl {
    @Inject
    private BrowseIndexConfig[] browseIndexes;
    @Inject
    private BrowseLinkConfig[] browseLinks;
}
