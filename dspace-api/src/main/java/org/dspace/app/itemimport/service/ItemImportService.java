/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport.service;

import org.dspace.app.itemimport.BatchUpload;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Import items into DSpace. The conventional use is upload files by copying
 * them. DSpace writes the item's bitstreams into its assetstore. Metadata is
 * also loaded to the DSpace database.
 * <P>
 * A second use assumes the bitstream files already exist in a storage
 * resource accessible to DSpace. In this case the bitstreams are 'registered'.
 * That is, the metadata is loaded to the DSpace database and DSpace is given
 * the location of the file which is subsumed into DSpace.
 * <P>
 * The distinction is controlled by the format of lines in the 'contents' file.
 * See comments in processContentsFile() below.
 * <P>
 * Modified by David Little, UCSD Libraries 12/21/04 to
 * allow the registration of files (bitstreams) into DSpace.
 */
public interface ItemImportService {


    /**
     * 
     * @param c DSpace Context
     * @param mycollections List of Collections
     * @param sourceDir source location
     * @param mapFile map file
     * @param template whether to use template item
     * @param messageStream sink for messages to the user.
     * @throws Exception if error 
     */
    public void addItemsAtomic(Context c, List<Collection> mycollections,
            String sourceDir, String mapFile, boolean template,
            PrintStream messageStream)
            throws Exception;

    /**
     * Add items
     * @param c DSpace Context
     * @param mycollections List of Collections
     * @param sourceDir source location
     * @param mapFile map file
     * @param template whether to use template item
     * @param messageStream sink for messages to the user.
     * @throws Exception if error
     */
    public void addItems(Context c, List<Collection> mycollections,
            String sourceDir, String mapFile, boolean template,
            PrintStream messageStream)
            throws Exception;

    /**
     * Unzip a file
     * @param zipfile file
     * @param messageStream sink for messages to the user.
     * @return unzip location
     * @throws IOException if error
     */
    public String unzip(File zipfile, PrintStream messageStream) throws IOException;

    /**
     * Unzip a file to a destination
     * @param zipfile file
     * @param destDir destination directory
     * @param messageStream sink for messages to the user.
     * @return unzip location
     * @throws IOException if error
     */
    public String unzip(File zipfile, String destDir, PrintStream messageStream) throws IOException;

    /**
     * Unzip a file in a specific source directory
     * @param sourcedir source directory
     * @param zipfilename file name
     * @param messageStream sink for messages to the user.
     * @return unzip location
     * @throws IOException if error
     */
    public String unzip(String sourcedir, String zipfilename, PrintStream messageStream) throws IOException;

    /**
     * Given a local file, or public URL to a zip file that has the Simple
     * Archive Format, this method imports the contents to DSpace.
     *
     * @param url The path to local file or the public URL of the zip file
     * @param owningCollection The owning collection the items will belong to
     * @param otherCollections The collections the created items will be inserted to, apart from the owning one
     * @param resumeDir In case of a resume request, the directory that contains the old map file and data
     * @param inputType The input type of the data (bibtex, csv, etc.), in case of local file
     * @param context The context
     * @param template whether to use template item
     * @param messageStream sink for messages to the user.
     * @throws Exception if error
     */
    public void processUIImport(String url, Collection owningCollection,
            String[] otherCollections, String resumeDir, String inputType,
            Context context, boolean template, PrintStream messageStream)
            throws Exception;

    /**
     * Since the BTE batch import is done in a new thread we are unable to communicate
     * with calling method about success or failure. We accomplish this
     * communication with email instead. Send a success email once the batch
     * import is complete
     *
     * @param context
     *            - the current Context
     * @param eperson
     *            - eperson to send the email to
     * @param fileName
     *            - the filepath to the mapfile created by the batch import
     * @throws MessagingException if error
     */
    public void emailSuccessMessage(Context context, EPerson eperson,
            String fileName) throws MessagingException;

    /**
     * Since the BTE batch import is done in a new thread we are unable to communicate
     * with calling method about success or failure. We accomplis this
     * communication with email instead. Send an error email if the batch
     * import fails
     *
     * @param eperson
     *            - EPerson to send the error message to
     * @param error
     *            - the error message
     * @throws MessagingException if error
     */
    public void emailErrorMessage(EPerson eperson, String error)
            throws MessagingException;


    /**
     * Get imports available for a person
     * @param eperson EPerson object
     * @return List of batch uploads
     * @throws Exception if error
     */
    public List<BatchUpload> getImportsAvailable(EPerson eperson)
            throws Exception;

    /**
     * Get import upload directory
     * @param ePerson EPerson object
     * @return directory
     * @throws Exception if error 
     */
    public String getImportUploadableDirectory(EPerson ePerson)
            throws Exception;

    /**
     * Delete a batch by ID
     * @param c DSpace Context
     * @param uploadId identifier
     * @param messageStream sink for messages to the user.
     * @throws Exception if error
     */
    public void deleteBatchUpload(Context c, String uploadId, PrintStream messageStream) throws Exception;

    /**
     * Replace items
     * @param c DSpace Context
     * @param mycollections List of Collections
     * @param sourcedir source directory
     * @param mapfile map file
     * @param template whether to use template item
     * @param messageStream sink for messages to the user.
     * @throws Exception if error
     */
    public void replaceItems(Context c, List<Collection> mycollections,
            String sourcedir, String mapfile, boolean template,
            PrintStream messageStream)
            throws Exception;

    /**
     * Delete items via mapfile
     * @param c DSpace Context
     * @param mapfile map file
     * @param messageStream sink for messages to the user.
     * @throws Exception if error
     */
    public void deleteItems(Context c, String mapfile, PrintStream messageStream)
            throws Exception;

    /**
     * In this method, the BTE is instantiated. THe workflow generates the DSpace files
     * necessary for the upload, and the default item import method is called
     * @param c The context
     * @param mycollections The collections the items are inserted to
     * @param sourceDir The path to the file to read data from
     * @param mapFile The path to the map file to be generated
     * @param template whether to use collection template item as starting point
     * @param bteInputType The type of the input data (bibtex, csv, etc.)
     * @param workingDir The path to create temporary files (for command line or UI based)
     * @param messageStream a sink for messages to the user.
     * @throws Exception if error occurs
     */
    public void addBTEItems(Context c, List<Collection> mycollections,
            String sourceDir, String mapFile, boolean template,
            String bteInputType, String workingDir, PrintStream messageStream)
            throws Exception;

    /**
     * Get temporary work directory
     * @return directory as string
     */
    public String getTempWorkDir();

    /**
     * Get temporary work directory (as File)
     * @return directory as File
     * @throws java.io.IOException if the directory cannot be created.
     */
    public File getTempWorkDirFile() throws IOException;

    /**
     * Cleanup
     * @param messageStream sink for messages to the user.
     */
    public void cleanupZipTemp(PrintStream messageStream);

    /**
     * Set test flag
     * @param isTest true or false 
     */
    public void setTest(boolean isTest);

    /**
     * Set resume flag
     * @param isResume true or false 
     */
    public void setResume(boolean isResume);

    /**
     * Set use workflow
     * @param useWorkflow whether to enable workflow
     */
    public void setUseWorkflow(boolean useWorkflow);

    /**
     * @param useWorkflowSendMail whether to send mail
     */
    public void setUseWorkflowSendEmail(boolean useWorkflowSendMail);

    /**
     * Set quiet flag
     * @param isQuiet true or false
     */
    public void setQuiet(boolean isQuiet);
}
