/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.handle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.inject.Inject;
import net.handle.hdllib.AuthenticationInfo;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.HandleException;

import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.SecretKeyAuthenticationInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;

/**
 * Client for a Handle resolver.
 *
 * @author mwood
 */
public class ExternalHandleServiceImpl
        extends AbstractHandleServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    private final HandleResolver handleResolver;

    private static final String C_HANDLE_SERVER_USER = "handle.server.user";
    private static final String C_HANDLE_SERVER_SECRET_KEY = "handle.server.key.secret";

    @Inject
    private DSpaceObjectService dspaceObjectService;

    public ExternalHandleServiceImpl() {
        super();
        handleResolver = new HandleResolver();
    }

    @Override
    public String resolveUrlToHandle(Context context, String url) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public String createHandle(Context context, DSpaceObject dso)
            throws SQLException {
        String handle = getPrefix() + "/" + handleDAO.getNextHandleSuffix(context); // XXX create row instead?
        return createHandle(context, dso, handle);
    }

    @Override
    public String createHandle(Context context, DSpaceObject dso, String suppliedHandle)
            throws SQLException {
        HandleValue[] values = new HandleValue[1];
        values[0].setData(resolveToURL(context, suppliedHandle).getBytes());
        values[0].setAdminCanRead(true);
        values[0].setAdminCanWrite(true);
        values[0].setAnyoneCanRead(true);
        values[0].setAnyoneCanWrite(false);
        // XXX setType?

        String handleUser = configurationService.getProperty(C_HANDLE_SERVER_USER);
        AuthenticationInfo authentication = new SecretKeyAuthenticationInfo(
                handleUser.getBytes(), 0, getSecretKey()); // FIXME index

        CreateHandleRequest req = new CreateHandleRequest(suppliedHandle.getBytes(), values, authentication);
        try {
            handleResolver.processRequest(req);
        } catch (HandleException ex) {
            LOG.error("HandleException:  {}", ex.getMessage());
            throw new SQLException(ex.getMessage(), ex);
        }

        // TODO create handle database row?

        return suppliedHandle;
    }

    @Override
    public String createHandle(Context context, DSpaceObject dso, String suppliedHandle, boolean force) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public void unbindHandle(Context context, DSpaceObject dso) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public DSpaceObject resolveToObject(Context context, String handle)
            throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public List<String> getHandlesForPrefix(Context context, String prefix) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public long countHandlesByPrefix(Context context, String prefix) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public int updateHandlesWithNewPrefix(Context context, String newPrefix, String oldPrefix) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public void modifyHandleDSpaceObject(Context context, String handle, DSpaceObject newOwner) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public int countTotal(Context context) {
        // TODO see ListHandlesRequest.
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    protected Handle findHandleInternal(Context context, String handle)
            throws SQLException {
        HandleValue[] handleRecords;
        try {
            handleRecords = handleResolver.resolveHandle(handle);
        } catch (HandleException ex) {
            LOG.error("Failed to resolve handle '{}'", ex);
            return null;
        }
        if (handleRecords.length <= 0) {
            LOG.debug("Handle '{}' did not resolve", handle);
            return null;
        }
        String value = handleRecords[0].getDataAsString();
        // uuid = parsed out of 'value'
        UUID uuid = UUID.fromString(value); // FIXME what is stored in Handle?

        // Look up DSO by 'value'.
        DSpaceObject dso = dspaceObjectService.find(context, uuid);

        Handle handleObject = new Handle();
        handleObject.setHandle(handle);
        handleObject.setDSpaceObject(dso);
        handleObject.setResourceTypeId(dso.getType());
        return handleObject;
    }

    /**
     * Read the Handle user's secret key from the configured path.
     *
     * @return the key as read.
     * @throws SQLException if the key file is unavailable or unreadable.
     */
    private byte[] getSecretKey()
            throws SQLException {
        byte[] secretKey;
        String keyPath = configurationService.getProperty(C_HANDLE_SERVER_SECRET_KEY);
        try (InputStream keyStream = new FileInputStream(keyPath)) {
            secretKey = keyStream.readAllBytes();
        } catch (FileNotFoundException ex) {
            LOG.error("Handle server secret key {} not read", keyPath, ex.getMessage());
            throw new SQLException("Handle operation aborted");
        } catch (IOException ex) {
            LOG.error("Unable to read handle server secret key {}", keyPath, ex.getMessage());
            throw new SQLException("Handle operation aborted");
        }
        return secretKey;
    }
}
