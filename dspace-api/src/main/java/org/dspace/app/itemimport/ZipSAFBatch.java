/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Represent a batch of Items archived to a Zip archive in Simple Archive Format
 * layout.
 *
 * @author mwood
 */
public class ZipSAFBatch
        implements SAFBatch
{
    private final ZipFile archive;
    private final ArrayList<String> itemNames;

    /**
     * Instantiate a batch in the Zip archive at a given path.
     * @param path the archive.
     * @throws IOException passed through.
     */
    public ZipSAFBatch(String path)
            throws IOException
    {
        this.archive = new ZipFile(path);
        this.itemNames = new ArrayList<>();

        Enumeration<? extends ZipEntry> entries = archive.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();

            // Skip files; we only want to enumerate directories.
            if (!entry.isDirectory())
                continue;

            File entryPath = new File(entry.getName());
            if (null == entryPath.getParent()) // We only want to enumerate *top* directories.
                itemNames.add(entry.getName());
        }
    }

    @Override
    public InputStream getStream(String itemName, String componentName)
            throws IOException
    {
        return archive.getInputStream(archive.getEntry(itemName + '/' + componentName));
    }

    @Override
    public List<String> itemFileNames(String itemName)
    {
        List<String> names = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = archive.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory())
                continue; // Skip *any* directory since an item should not have subdirectories.

            if (entry.getName().startsWith(itemName + '/'))
                names.add(itemName.split("/", 2)[1]);
        }

        return names;
    }

    @Override
    public Iterator<SAFitem> iterator()
    {
        return new ItemIterator(this, itemNames);
    }
}
