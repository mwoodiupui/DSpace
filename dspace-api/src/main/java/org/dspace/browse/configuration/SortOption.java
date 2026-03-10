package org.dspace.browse.configuration;

import org.dspace.content.MetadataFieldName;

/**
 * Configuration of sorting for a given metadata field.
 *
 * @author mwood
 */
public class SortOption {
    private final String name;
    private final MetadataFieldName field;
    private final String normalization; // TODO enumerate title text date {other}

    public SortOption(String name, MetadataFieldName field, String normalization) {
        this.name = name;
        this.field = field;
        this.normalization = normalization;
    }
}
