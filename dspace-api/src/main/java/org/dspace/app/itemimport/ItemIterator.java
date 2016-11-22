/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import java.util.Collection;
import java.util.Iterator;

/**
 * An Iterator for items contained in a SAFBatch.
 */
class ItemIterator implements Iterator<SAFitem>
{
    private final Iterator<String> itemIterator;

    private final SAFBatch batch;

    /**
     * Instantiate an item iterator for a batch.
     * @param aBatch the batch whose items will be iterated.
     */
    ItemIterator(SAFBatch aBatch, Collection<String> itemNames)
    {
        batch = aBatch;
        itemIterator = itemNames.iterator();
    }

    @Override
    public boolean hasNext()
    {
        return itemIterator.hasNext();
    }

    @Override
    public SAFitem next()
    {
        String nextName = itemIterator.next();
        return new SAFitem(nextName, batch);
    }
}
