package org.dspace.browse.configuration;

/**
 * Configuration of an Item browse index.
 * @author mwood
 */
public class ItemBrowseIndexConfig
        extends BrowseIndexConfig {
    public ItemBrowseIndexConfig(String name, SortOption sortOption, String order) {
        super(name, order, sortOption);
    }
}
