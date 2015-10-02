/*
 * Copyright 2015 Mark H. Wood.
 */

package org.dspace.handle;

import java.sql.SQLException;
import net.handle.api.HSAdapter;
import net.handle.api.HSAdapterFactory;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleValue;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement HandleService using an external Handle resolver via the Handle protocol.
 *
 * This depends on the CNRI Handle Client for Java, which must be manually added
 * to your Maven repository.
 *
 * @author mhwood
 */
public class ExternalHandleServiceImpl
        extends HandleServiceImpl
{
    private static final Logger log = LoggerFactory.getLogger(ExternalHandleServiceImpl.class);

    /** Handle type for URLs. */
    private static final String URL = "URL";

    /** The DSpace URL mapped to this Handle. */
    private static final int URL_INDEX = 1;

    @Override
    public String createHandle(Context context, DSpaceObject dso)
            throws SQLException
    {
        // Create a DSpace Handle object.
        Handle handle = handleDAO.create(context, new Handle());
        String handleName = createId(handle.getId());

        handle.setHandle(handleName);
        handle.setDSpaceObject(dso);
        dso.setHandle(handle);
        handle.setResourceTypeId(dso.getType());

        handleDAO.save(context, handle);

        // Register the DSO with the Handle resolver.
        HSAdapter hsAdapter = HSAdapterFactory.newInstance();
        try {
            HandleValue[] values = new HandleValue[1];
            values[0] = hsAdapter.createHandleValue(URL_INDEX, URL,
                    resolveToURL(context, handleName));
            hsAdapter.createHandle(handleName, values);
        } catch (HandleException ex) {
            log.warn("Handle {} for {} {} not registered with the resolver:  {}",
                    new String[] { handleName,
                        org.dspace.core.Constants.typeText[dso.getType()],
                        dso.getID().toString(),
                        ex.getMessage()});
        }

        if (log.isDebugEnabled())
            log.debug("Created new handle for {} (ID={}) {}",
                    new String[] { Constants.typeText[dso.getType()],
                        dso.getID().toString(),
                        handleName });

        return handleName;
    }

    @Override
    public String createHandle(Context context, DSpaceObject dso,
            String suppliedHandle, boolean force)
            throws SQLException, IllegalStateException
    {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
    }

    @Override
    public void unbindHandle(Context context, DSpaceObject dso)
            throws SQLException
    {
        // Remove the handle from the external service.
        HSAdapter hsAdapter = HSAdapterFactory.newInstance();
        String handle = dso.getHandle();
        if (null == handle)
        {
            log.info("Cannot unbind Handle of {} {} because it has none.",
                    Constants.typeText[dso.getType()], dso.getID().toString());
            return;
        }

        try {
            hsAdapter.deleteHandle(handle);
        } catch (HandleException ex) {
            log.error("Unable to unbind Handle {}:  {}", handle, ex.getMessage());
        }

        // Remove the handle from the dso.
        super.unbindHandle(context, dso);
    }

    @Override
    public int updateHandlesWithNewPrefix(Context context, String newPrefix,
            String oldPrefix)
            throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
    }

    @Override
    public void modifyHandleDSpaceObject(Context context, String handle,
            DSpaceObject newOwner)
            throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
    }
}
