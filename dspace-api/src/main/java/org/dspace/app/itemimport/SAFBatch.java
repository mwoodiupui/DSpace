/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Represent an abstract batch of items to be ingested.
 * Implementations will support various storage options (directory tree, Zip
 * archive, etc.)
 *
 * @author mwood
 */
interface SAFBatch
        extends Iterable<SAFitem>
{
    /**
     * Enable walking the collection of Items.
     * @return iterator to return each archived item in turn.
     */
    @Override
    Iterator<SAFitem> iterator();

    /**
     * Get a stream to read the content of a "file" from the archive.
     *
     * @param itemName name of the item containing the "file".
     * @param componentName name of the "file" itself.
     * @return a stream from which the named "file" may be read.
     * @throws IOException passed through.
     */
    InputStream getStream(String itemName, String componentName)
            throws IOException;

    /**
     * Get the names of files within a named item.
     *
     * @param itemName name of the item.
     * @return names of files in that item.
     */
    List<String> itemFileNames(String itemName);
}
