package org.dspace.browse.configuration;

import org.dspace.content.MetadataFieldName;

/**
 * Additional metadata field for an index.
 *
 * @author mwood
 */
public class BrowseLinkConfig {
    private final String name;
    private final MetadataFieldName field;

    public BrowseLinkConfig(String name, MetadataFieldName field) {
        this.name = name;
        this.field = field;
    }
}
