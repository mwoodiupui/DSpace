/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent a single bitstream of an Item, with its options.
 *
 * @author mwood
 */
class ContentStream
{
    String filename;
    final Map<String, String> options = new HashMap<>(10);
}
