/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represent a batch of Items archived to a directory tree in Simple Archive Format
 * layout.
 *
 * @author mwood
 */
public class FilesystemSAFBatch
        implements SAFBatch
{
    private final File archive;

    private final ArrayList<String> itemNames;

    /**
     * Instantiate a batch on a given path.
     *
     * @param path path to the directory containing the batch tree.
     * @throws IOException passed through.
     */
    public FilesystemSAFBatch(String path)
            throws IOException
    {
        this.archive = new File(path);
        this.itemNames = new ArrayList<>();

        for (File entry : archive.listFiles())
        {
            // Skip files; we only want to enumerate directories.
            if (!entry.isDirectory())
                continue;

            itemNames.add(entry.getName());
        }
    }

    @Override
    public InputStream getStream(String itemName, String componentName)
            throws IOException
    {
        return new FileInputStream(new File(new File(archive, itemName), componentName));
    }

    @Override
    public List<String> itemFileNames(String itemName)
    {
        List<String> names = new ArrayList<>();
        File item = new File(archive, itemName);
        for (File entry : item.listFiles())
        {
            if (entry.isDirectory())
                continue; // Skip *any* directory since an item should not have subdirectories.

            names.add(entry.getName());
        }

        return names;
    }

    @Override
    public Iterator<SAFitem> iterator()
    {
        return new ItemIterator(this, itemNames);
    }
}
