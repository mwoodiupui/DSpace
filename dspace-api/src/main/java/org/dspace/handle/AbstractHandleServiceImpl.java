/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.handle.dao.HandleDAO;
import org.dspace.handle.service.HandleService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Common code for HandleService implementations.
 *
 * @author mwood
 */
public abstract class AbstractHandleServiceImpl
        implements HandleService {
    private static final Logger log = LogManager.getLogger();

    /** Prefix registered to no one */
    static final String EXAMPLE_PREFIX = "123456789";

    @Inject
    protected ConfigurationService configurationService;

    @Inject
    protected HandleDAO handleDAO;

    protected static final Pattern[] IDENTIFIER_PATTERNS = {
        Pattern.compile("^hdl:(.*)$"),
        Pattern.compile("^info:hdl/(.*)$"),
        Pattern.compile("^https?://hdl\\.handle\\.net/(.*)$"),
        Pattern.compile("^https?://.+/handle/(.*)$")
    };

    @Override
    public String getCanonicalPrefix() {
        // Let the admin define a new prefix, if not then we'll use the
        // CNRI default. This allows the admin to use "hdl:" if they want to or
        // use a locally branded prefix handle.myuni.edu.
        String handlePrefix = configurationService.getProperty("handle.canonical.prefix", "https://hdl.handle.net/");
        return handlePrefix;
    }

    @Override
    public String getCanonicalForm(String handle) {
        return getCanonicalPrefix() + handle;
    }

    @Override
    public String getPrefix() {
        String prefix = configurationService.getProperty("handle.prefix");
        if (StringUtils.isBlank(prefix)) {
            prefix = EXAMPLE_PREFIX; // XXX no good way to exit cleanly
            log.error("handle.prefix is not configured; using {}", prefix);
        }
        return prefix;
    }

    @Override
    public String parseHandle(String identifier) {
        if (identifier == null) {
            return null;
        }
        if (identifier.startsWith(getPrefix() + "/")) {
            // prefix is the equivalent of 123456789 in 123456789/???; don't strip
            return identifier;
        }
        String canonicalPrefix = configurationService.getProperty("handle.canonical.prefix");
        if (identifier.startsWith(canonicalPrefix + "/")) {
            // prefix is the equivalent of https://hdl.handle.net/ in https://hdl.handle.net/123456789/???; strip
            return StringUtils.stripStart(identifier, canonicalPrefix);
        }
        for (Pattern pattern : IDENTIFIER_PATTERNS) {
            Matcher matcher = pattern.matcher(identifier);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        // Check additional prefixes supported in the config file
        String[] additionalPrefixes = getAdditionalPrefixes();
        for (String additionalPrefix : additionalPrefixes) {
            if (identifier.startsWith(additionalPrefix + "/")) {
                // prefix is the equivalent of 123456789 in 123456789/???; don't strip
                return identifier;
            }
        }
        return null;
    }

    @Override
    public String[] getAdditionalPrefixes() {
        return configurationService.getArrayProperty("handle.additional.prefixes");
    }

    @Override
    public String findHandle(Context context, DSpaceObject dso) throws SQLException {
        List<Handle> handles = dso.getHandles();
        if (CollectionUtils.isEmpty(handles)) {
            return null;
        } else {
            //TODO: Move this code away from the HandleService & into the Identifier provider
            //Attempt to retrieve a handle that does NOT look like {handle.part}/{handle.part}.{version}
            String result = handles.iterator().next().getHandle();
            for (Handle handle : handles) {
                //Ensure that the handle doesn't look like this 12346/213.{version}
                //If we find a match that indicates that we have a proper handle
                if (!handle.getHandle().matches(".*/.*\\.\\d+")) {
                    result = handle.getHandle();
                }
            }
            return result;
        }
    }

    /**
     * Look up a Handle object by name.
     *
     * @param context current DSpace session.
     * @param handle name of the object.
     * @return the object, or null if not found.
     * @throws java.sql.SQLException passed through.
     */
    abstract protected Handle findHandleInternal(Context context, String handle)
            throws SQLException;

    @Override
    public String createHandle(Context context, DSpaceObject dso, String suppliedHandle)
            throws SQLException, IllegalStateException {
        return createHandle(context, dso, suppliedHandle, false);
    }

    @Override
    public String resolveToURL(Context context, String handle)
            throws SQLException {
        Handle dbhandle = findHandleInternal(context, handle);
        if (dbhandle == null) {
            return null;
        }
        String url = configurationService.getProperty("dspace.ui.url") + "/handle/" + handle;
        log.debug("Resolved {} to {}", handle, url);
        return url;
    }

    /**
     * Create/mint a new handle id.
     *
     * @param context DSpace Context
     * @return A new handle id
     * @throws SQLException If a database error occurs
     */
    protected String createId(Context context) throws SQLException {
        // Get configured prefix
        String handlePrefix = getPrefix();
        // Get next available suffix (as a Long, since DSpace uses an incrementing sequence)
        Long handleSuffix = handleDAO.getNextHandleSuffix(context);
        return handlePrefix + (handlePrefix.endsWith("/") ? "" : "/") + handleSuffix.toString();
    }
}
