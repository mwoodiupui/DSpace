/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represent an archived Item in Simple Archive Format.
 *
 * @author mwood
 */
public class SAFitem
{
    private static final Logger LOG = LoggerFactory.getLogger(SAFitem.class);

    /** Match the names of metadata files. */
    private static final Pattern METADATA_FILENAME_MATCHER
            = Pattern.compile("metadata_(.+)\\.xml", 0);

    /** The batch that holds this item. */
    private final SAFBatch batch;

    /** The name of this item. */
    private final String myName;

    private final ArrayList<String> collections = new ArrayList<>();

    private final ArrayList<Metadatum> metadata = new ArrayList<>();

    private final ArrayList<ContentStream> contents = new ArrayList<>();

    /**
     * Initialize an item within a batch.
     *
     * @param itemName name of this item.
     * @param aBatch the batch which contains me.
     */
    SAFitem(String itemName, SAFBatch aBatch)
    {
        myName = itemName;
        batch = aBatch;
    }

    /**
     * Get the list of collections to which this item belongs.
     *
     * @return collection names.
     * @throws IOException passed through.
     */
    Collection<String> getCollections()
            throws IOException
    {
        if (collections.isEmpty())
        {
            try (
                    InputStream collectionsStream
                            = batch.getStream(myName, "collections");
                    InputStreamReader collectionsStreamReader
                            = new InputStreamReader(collectionsStream,
                                    StandardCharsets.UTF_8);
                    BufferedReader collectionsReader
                            = new BufferedReader(collectionsStreamReader)
                    )
            {
                while (true)
                {
                    String line = collectionsReader.readLine();
                    if (null == line) break;
                    collections.add(line.trim());
                }
            }
        }

        return (Collection) collections.clone();
    }

    /**
     * Get parsed content of the 'contents' file.
     *
     * @return records describing each bitstream.
     * @throws IOException passed through.
     */
    Collection<ContentStream> getContents()
            throws IOException
    {
        if (contents.isEmpty())
        {
            try (InputStream contentsStream = batch.getStream(myName, "contents"))
            {
                readContents(contentsStream);
            }
        }

        return contents;
    }

    /**
     * Access all of the item's metadata values.
     *
     * @return the item's metadata.
     * @throws IOException passed through.
     * @throws SAXException passed through.
     */
    Collection<Metadatum> getMetadata()
            throws IOException, SAXException
    {
        if (metadata.isEmpty())
        {
            InputStream entry;

            entry = batch.getStream(myName, "dublin_core.xml");
            if (null != entry)
                loadMetadata(entry, "dc");

            for (String fileName : batch.itemFileNames(myName))
            {
                Matcher matcher = METADATA_FILENAME_MATCHER.matcher(fileName);
                if (!matcher.matches())
                    continue;
                loadMetadata(batch.getStream(myName, fileName), matcher.group(1));
            }
        }

        return (Collection) metadata.clone();
    }

    /**
     * Read a metadata document from an item.
     *
     * @param inputStream XML document whence to read the metadata records.
     * @param schemaName name of the schema used in this document.
     * @throws IOException passed through.
     * @throws SAXException passed through.
     */
    private void loadMetadata(InputStream inputStream, String schemaName)
            throws IOException, SAXException
    {
        Document doc;
        try
        {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source source = new StreamSource(SAFitem.class.getResourceAsStream(
                    "batchMetadata.xsd"));
            Schema schema = sf.newSchema(source);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setSchema(schema);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(inputStream);
        } catch (ParserConfigurationException ex)
        {
            LOG.error("Programmer error!", ex);
            throw new IOException("Programmer error", ex);
        }
        NodeList dcvalues = doc.getElementsByTagName("dcvalue");
        for (int i = 0; i < dcvalues.getLength(); i++)
        {
            Node dcvalue = dcvalues.item(i);
            NamedNodeMap attributes = dcvalue.getAttributes();
            Node node;
            Metadatum datum = new Metadatum();
            datum.schema = schemaName;
            node = attributes.getNamedItem("element");
            if (null != node)
            {
                datum.element = node.getNodeValue();
            }
            node = attributes.getNamedItem("qualifier");
            if (null != node)
            {
                datum.qualifier = node.getNodeValue();
            }
            node = attributes.getNamedItem("language");
            if (null != node)
            {
                datum.language = node.getNodeValue();
            }
            metadata.add(datum);
        }
    }

    /** Read and parse the content of an item's 'contents' file.
     *
     * @param contentsStream whence to read the contents.
     * @throws java.io.IOException passed through.
     */
    private void readContents(InputStream contentsStream)
            throws IOException
    {
        try (
                InputStreamReader contentsStreamReader
                        = new InputStreamReader(contentsStream,
                                StandardCharsets.UTF_8);
                BufferedReader contentsReader
                        = new BufferedReader(contentsStreamReader)
                )
        {
            while (true)
            {
                String line = contentsReader.readLine();
                if (null == line) break;
                String[] atoms = line.trim().split("[\\t]");
                ContentStream content = new ContentStream();
                content.filename = atoms[0];
                for (int i = 1; i < atoms.length; i++)
                {
                    String[] kv = atoms[i].split(":", 2);
                    String key, value;
                    key = kv[0];
                    if (kv.length > 1)
                        value = kv[1].trim();
                    else
                        value = null;
                    content.options.put(key, value);
                }
                contents.add(content);
            }
        }
    }
}
