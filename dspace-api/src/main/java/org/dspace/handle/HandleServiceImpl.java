/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.service.SiteService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.dao.HandleDAO;
import org.dspace.handle.service.HandleService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Interface to the <a href="https://www.handle.net" target=_new>CNRI Handle
 * System</a>.
 *
 * <p>
 * Currently, this class simply maps handles to local facilities; handles which
 * are owned by other sites (including other DSpaces) are treated as
 * non-existent.
 * </p>
 *
 * @author Peter Breton
 */
public class HandleServiceImpl
        extends AbstractHandleServiceImpl
        implements HandleService {
    /**
     * log category
     */
    private static final Logger log = LogManager.getLogger();

    /**
     * Constructor
     */
    protected HandleServiceImpl() {
    }

    @Override
    public String resolveUrlToHandle(Context context, String url)
        throws SQLException {
        String dspaceUrl = configurationService.getProperty("dspace.ui.url")
            + "/handle/";
        String handleResolver = getCanonicalPrefix();

        String handle = null;

        if (url.startsWith(dspaceUrl)) {
            handle = url.substring(dspaceUrl.length());
        }

        if (url.startsWith(handleResolver)) {
            handle = url.substring(handleResolver.length());
        }

        if (null == handle) {
            return null;
        }

        // remove trailing slashes
        while (handle.startsWith("/")) {
            handle = handle.substring(1);
        }
        Handle dbhandle = findHandleInternal(context, handle);

        return (null == dbhandle) ? null : handle;
    }

    @Override
    public String createHandle(Context context, DSpaceObject dso)
        throws SQLException {
        Handle handle = handleDAO.create(context, new Handle());
        String handleId = createId(context);

        handle.setHandle(handleId);
        handle.setDSpaceObject(dso);
        dso.addHandle(handle);
        int dsoType = dso.getType();
        handle.setResourceTypeId(dsoType);
        handleDAO.save(context, handle);

        log.debug("Created new handle for {} (ID={}) {}",
            () -> Constants.typeText[dsoType],
            dso::getID,
            () -> handleId);

        return handleId;
    }

    @Override
    public String createHandle(Context context, DSpaceObject dso,
                               String suppliedHandle, boolean force) throws SQLException, IllegalStateException {
        //Check if the supplied handle is already in use -- cannot use the same handle twice
        Handle handle = findHandleInternal(context, suppliedHandle);
        int dsoType = dso.getType();
        if (handle != null && handle.getDSpaceObject() != null) {
            //Check if this handle is already linked up to this specified DSpace Object
            if (handle.getDSpaceObject().getID().equals(dso.getID())) {
                //This handle already links to this DSpace Object -- so, there's nothing else we need to do
                return suppliedHandle;
            } else {
                //handle found in DB table & already in use by another existing resource
                throw new IllegalStateException(
                    "Attempted to create a handle which is already in use: " + suppliedHandle);
            }
        } else if (handle != null && handle.getResourceTypeId() != null) {
            // If there is a 'resource_type_id' (but 'resource_id' is empty),
            // then the object using this handle was previously unbound (see
            // unbindHandle() method) -- likely because object was deleted.
            int previousType = handle.getResourceTypeId();

            //Since we are restoring an object to a pre-existing handle, double
            // check we are restoring the same *type* of object (e.g. we will
            // not allow an Item to be restored to a handle previously used by
            // a Collection).
            if (previousType != dsoType) {
                throw new IllegalStateException("Attempted to reuse a handle previously used by a " +
                                                    Constants.typeText[previousType] + " for a new " +
                                                    Constants.typeText[dsoType]);
            }
        } else if (handle == null) {
            //if handle not found, create it
            //handle not found in DB table -- create a new table entry
            handle = handleDAO.create(context, new Handle());
            handle.setHandle(suppliedHandle);
        }

        handle.setResourceTypeId(dsoType);
        handle.setDSpaceObject(dso);
        dso.addHandle(handle);
        handleDAO.save(context, handle);

        log.debug("Created new handle for {} (ID={}) {}",
            () -> Constants.typeText[dsoType],
            dso::getID,
            () -> suppliedHandle);

        return suppliedHandle;
    }

    @Override
    public void unbindHandle(Context context, DSpaceObject dso)
        throws SQLException {
        Iterator<Handle> handles = dso.getHandles().iterator();
        if (handles.hasNext()) {
            while (handles.hasNext()) {
                final Handle handle = handles.next();
                handles.remove();
                //Only set the "resouce_id" column to null when unbinding a handle.
                // We want to keep around the "resource_type_id" value, so that we
                // can verify during a restore whether the same *type* of resource
                // is reusing this handle!
                handle.setDSpaceObject(null);


                handleDAO.save(context, handle);

                log.debug("Unbound Handle {} from object {} id={}",
                    () -> handle.getHandle(),
                    () -> Constants.typeText[dso.getType()],
                    () -> dso.getID());
            }
        } else {
            log.trace(
                "Cannot find Handle entry to unbind for object {} id={}. Handle could have been unbound before.",
                    Constants.typeText[dso.getType()], dso.getID());
        }
    }

    @Override
    public DSpaceObject resolveToObject(Context context, String handle)
        throws IllegalStateException, SQLException {
        Handle dbhandle = findHandleInternal(context, handle);
        // check if handle was allocated previously, but is currently not
        // associated with a DSpaceObject
        // (this may occur when 'unbindHandle()' is called for an obj that was removed)
        if (dbhandle == null || (dbhandle.getDSpaceObject() == null)
            || (dbhandle.getResourceTypeId() == null)) {
            //if handle has been unbound, just return null (as this will result in a PageNotFound)
            return null;
        }

        return dbhandle.getDSpaceObject();
    }

    @Override
    public List<String> getHandlesForPrefix(Context context, String prefix)
        throws SQLException {
        List<Handle> handles = handleDAO.findByPrefix(context, prefix);
        List<String> handleStrings = new ArrayList<>(handles.size());
        for (Handle handle : handles) {
            handleStrings.add(handle.getHandle());
        }
        return handleStrings;
    }

    @Override
    public long countHandlesByPrefix(Context context, String prefix) throws SQLException {
        return handleDAO.countHandlesByPrefix(context, prefix);
    }

    @Override
    public int updateHandlesWithNewPrefix(Context context, String newPrefix, String oldPrefix) throws SQLException {
        return handleDAO.updateHandlesWithNewPrefix(context, newPrefix, oldPrefix);
    }

    @Override
    public void modifyHandleDSpaceObject(Context context, String handle, DSpaceObject newOwner) throws SQLException {
        Handle dbHandle = findHandleInternal(context, handle);
        if (dbHandle != null) {
            // Check if we have to remove the handle from the current handle list
            // or if object is alreday deleted.
            if (dbHandle.getDSpaceObject() != null) {
                // Remove the old handle from the current handle list
                dbHandle.getDSpaceObject().getHandles().remove(dbHandle);
            }
            // Transfer the current handle to the new object
            dbHandle.setDSpaceObject(newOwner);
            dbHandle.setResourceTypeId(newOwner.getType());
            newOwner.getHandles().add(0, dbHandle);
            handleDAO.save(context, dbHandle);
        }

    }

    ////////////////////////////////////////
    // Internal methods
    ////////////////////////////////////////
    /**
     * Find the database row corresponding to handle.
     *
     * @param context DSpace context
     * @param handle  The handle to resolve
     * @return The database row corresponding to the handle
     * @throws SQLException If a database error occurs
     */
    @Override
    protected Handle findHandleInternal(Context context, String handle)
        throws SQLException {
        if (handle == null) {
            throw new IllegalArgumentException("Handle is null");
        }

        return handleDAO.findByHandle(context, handle);
    }

    @Override
    public int countTotal(Context context) throws SQLException {
        return handleDAO.countRows(context);
    }
}
