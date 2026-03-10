package org.dspace.browse.configuration;

/**
 * Configuration of a browse index.
 *
 * @author mwood
 */
public class BrowseIndexConfig {
    private final String name;
    private final String order; // TODO enumeration:  asc | desc
    private final SortOption sortOption;

    public BrowseIndexConfig(String name, String order, SortOption sortOption) {
        this.name = name;
        this.order = order;
        this.sortOption = sortOption;
    }
}
