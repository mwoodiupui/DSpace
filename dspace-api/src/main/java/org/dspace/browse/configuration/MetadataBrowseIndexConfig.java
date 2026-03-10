/*
 * Copyright 2026 Indiana University.
 */
package org.dspace.browse.configuration;

import org.dspace.content.MetadataFieldName;

/**
 * Configuration of a metadata browse index.
 * @author mwood
 */
public class MetadataBrowseIndexConfig
        extends BrowseIndexConfig {
    private final MetadataFieldName[] fields;
    private final String type;

    public MetadataBrowseIndexConfig(String name, MetadataFieldName[] fields,
            String type, String order, SortOption sortOption) {
        super(name, order, sortOption);
        this.fields = fields;
        this.type = type; // TODO date title text {other}
    }
}
