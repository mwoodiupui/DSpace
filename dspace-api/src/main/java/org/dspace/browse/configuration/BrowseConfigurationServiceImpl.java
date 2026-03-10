package org.dspace.browse.configuration;

/**
 * Holder for the configuration of the browse functions.
 *
 * @author mwood
 */
public class BrowseConfigurationServiceImpl {
    private final BrowseIndexConfig[] browseIndexes;
    private final BrowseLinkConfig[] browseLinks;

    public BrowseConfigurationServiceImpl(BrowseIndexConfig[] indexes,
            BrowseLinkConfig[] links) {
        this.browseIndexes = indexes;
        this.browseLinks = links;
    }
}
