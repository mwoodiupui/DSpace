/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authenticate;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.services.factory.DSpaceServicesFactory;


/**
 * Implement this interface to participate in the stackable
 * authentication mechanism.  See the <code>AuthenticationManager</code>
 * class for details about configuring authentication handlers.
 * <p>
 * Each <em>authentication method</em> provides a way to map
 * "credentials" supplied by the client into a DSpace e-person.
 * "Authentication" is when the credentials are compared against some
 * sort of registry or other test of authenticity.
 * <p>
 * The DSpace instance may configure many authentication methods, in a
 * "stack".  The same credentials are passed to each method in turn
 * until one accepts them, so each method need only attempt to interpret
 * and validate the credentials and fail gracefully if they are not
 * appropriate for it.  The next method in the stack is then called.
 *
 * @author Larry Stone
 * @see org.dspace.authenticate.service.AuthenticationService
 */
public abstract class AuthenticationMethod {
    /** Log category */
    private static final Logger log = LogManager.getLogger();

    /**
     * Symbolic return values for authenticate() method:
     */

    /**
     * Authenticated OK, EPerson has been set.
     */
    public static final int SUCCESS = 1;

    /**
     * User exists, but credentials (<em>e.g.</em> password) don't match.
     */
    public static final int BAD_CREDENTIALS = 2;

    /**
     * Not allowed to login this way without X.509 certificate.
     */
    public static final int CERT_REQUIRED = 3;

    /**
     * User not found using this method.
     */
    public static final int NO_SUCH_USER = 4;

    /**
     * User or password is not appropriate for this method.
     */
    public static final int BAD_ARGS = 5;


    /**
     * Predicate, whether to allow new EPerson to be created.
     * The answer determines whether a new user is created when
     * the credentials describe a valid entity but there is no
     * corresponding EPerson in DSpace yet.
     * The EPerson is only created if authentication succeeds.
     *
     * @param context  DSpace context
     * @param request  HTTP request, in case it's needed. May be null.
     * @param username Username, if available.  May be null.
     * @return true if new ePerson should be created.
     * @throws SQLException if database error
     */
    abstract public boolean canSelfRegister(Context context,
                                   HttpServletRequest request,
                                   String username)
        throws SQLException;

    /**
     * Initialize a new EPerson record for a self-registered new user.
     * Set any data in the EPerson that is specific to this authentication
     * method.
     *
     * @param context DSpace context
     * @param request HTTP request, in case it's needed. May be null.
     * @param eperson newly created EPerson record - email + information from the
     *                registration form will have been filled out.
     * @throws SQLException if database error
     */
    abstract public void initEPerson(Context context,
                            HttpServletRequest request,
                            EPerson eperson)
        throws SQLException;

    /**
     * Should (or can) we allow the user to change their password.
     * Note that this means the password stored in the EPerson record, so if
     * <em>any</em> method in the stack returns true, the user is
     * allowed to change it.
     *
     * @param context  DSpace context
     * @param request  HTTP request, in case it's needed. May be null.
     * @param username Username, if available.  May be null.
     * @return true if this method allows user to change ePerson password.
     * @throws SQLException if database error
     */
    abstract public boolean allowSetPassword(Context context,
                                    HttpServletRequest request,
                                    String username)
        throws SQLException;

    /**
     * Predicate, is this an implicit authentication method.
     * An implicit method gets credentials from the environment (such as
     * an HTTP request or even Java system properties) rather than the
     * explicit username and password.  For example, a method that reads
     * the X.509 certificates in an HTTPS request is implicit.
     *
     * @return true if this method uses implicit authentication.
     */
    abstract public boolean isImplicit();

    /**
     * Get list of extra groups that user implicitly belongs to. Note that this
     * method will be invoked regardless of the authentication status of the
     * user (logged-in or not) e.g. a group that depends on the client
     * network-address.
     * <p>
     * It might make sense to implement this method by itself in a separate
     * authentication method that just adds special groups, if the code doesn't
     * belong with any existing auth method. The stackable authentication system
     * was designed expressly to separate functions into "stacked" methods to
     * keep your site-specific code modular and tidy.
     *
     * @param context A valid DSpace context.
     * @param request The request that started this operation, or null if not
     *                applicable.
     * @return array of EPerson-group IDs, possibly 0-length, but never
     * <code>null</code>.
     * @throws SQLException if database error
     */
    public List<Group> getSpecialGroups(Context context, HttpServletRequest request)
        throws SQLException {
        // Prevents anonymous users from being added to this group, and the second check
        // ensures they are password users
        try {
            if (context.getCurrentUser() != null
                && StringUtils.isNotBlank(
                EPersonServiceFactory.getInstance().getEPersonService().getPasswordHash(context.getCurrentUser())
                                     .toString())) {
                String groupName = DSpaceServicesFactory.getInstance()
                        .getConfigurationService()
                        .getProperty("authentication-" + getName() + ".login.specialgroup");
                if ((groupName != null) && !groupName.trim().isEmpty()) {
                    Group specialGroup = EPersonServiceFactory.getInstance().getGroupService()
                                                              .findByName(context, groupName);
                    if (specialGroup == null) {
                        // Oops - the group isn't there.
                        log.warn(LogHelper.getHeader(context,
                                                      getName() + "_specialgroup",
                                                      "Group defined in modules/authentication-password.cfg login" +
                                                          ".specialgroup does not exist"));
                        return Collections.EMPTY_LIST;
                    } else {
                        return Arrays.asList(specialGroup);
                    }
                }
            }
        } catch (Exception e) {
            log.error(LogHelper.getHeader(context, "getSpecialGroups", ""), e);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns true if the special groups returned by
     * {@link org.dspace.authenticate.AuthenticationMethod#getSpecialGroups(Context, HttpServletRequest)}
     * should be implicitly be added to the groups related to the current user. By
     * default this is true if the authentication method is the actual
     * authentication mechanism used by the user.
     * @param  context A valid DSpace context.
     * @param  request The request that started this operation, or null if not
     *                 applicable.
     * @return         true is the special groups must be considered, false
     *                 otherwise
     */
    public boolean areSpecialGroupsApplicable(Context context, HttpServletRequest request) {
        return getName().equals(context.getAuthenticationMethod());
    }

    /**
     * Authenticate the given or implicit credentials.
     * This is the heart of the authentication method: test the
     * credentials for authenticity, and if accepted, attempt to match
     * (or optionally, create) an <code>EPerson</code>.  If an <code>EPerson</code> is found it is
     * set in the <code>Context</code> that was passed.
     *
     * @param context  DSpace context, will be modified (ePerson set) upon success.
     * @param username Username (or email address) when method is explicit. Use null for
     *                 implicit method.
     * @param password Password for explicit auth, or null for implicit method.
     * @param realm    Realm is an extra parameter used by some authentication methods, leave null if
     *                 not applicable.
     * @param request  The HTTP request that started this operation, or null if not applicable.
     * @return One of:
     * SUCCESS, BAD_CREDENTIALS, CERT_REQUIRED, NO_SUCH_USER, BAD_ARGS
     * <p>Meaning:
     * <br>SUCCESS         - authenticated OK.
     * <br>BAD_CREDENTIALS - user exists, but credentials (e.g. passwd) don't match
     * <br>CERT_REQUIRED   - not allowed to login this way without X.509 cert.
     * <br>NO_SUCH_USER    - user not found using this method.
     * <br>BAD_ARGS        - user/pw not appropriate for this method
     * @throws SQLException if database error
     */
    abstract public int authenticate(Context context,
                            String username,
                            String password,
                            String realm,
                            HttpServletRequest request)
        throws SQLException;

    /**
     * Get an external login page to which to redirect.
     *
     * Returns URL (as string) to which to redirect to obtain
     * credentials (either password prompt or e.g. HTTPS port for client
     * cert.); null means no redirect.
     *
     * Note: Starting with DSpace 7, session logins will be managed through the REST
     * API.  Therefore, only authn providers with external login pages (such as Shibboleth)
     * should return a login page.
     *
     * @param context
     *  DSpace context, will be modified (ePerson set) upon success.
     *
     * @param request
     *  The HTTP request that started this operation, or null if not applicable.
     *
     * @param response
     *  The HTTP response from the servlet method.
     *
     * @return fully-qualified URL or null
     */
    abstract public String loginPageURL(Context context,
                               HttpServletRequest request,
                               HttpServletResponse response);

    /**
     * Returns a short name that uniquely identifies this authentication method.
     * This should be the method-specific part of the method's configuration
     * properties, such as "foo" in {@code authentication-foo.specialGroup}.
     *
     * @return The authentication method name
     */
    abstract public String getName();

    /**
     * Get whether the authentication method is being used.
     * @param context   The DSpace context
     * @param request   The current request
     * @return whether the authentication method is being used.
     */
    abstract public boolean isUsed(Context context, HttpServletRequest request);

    /**
     * Check if the given current password is valid to change the password of the
     * given ePerson
     * @param  context         The DSpace context
     * @param  ePerson         the ePerson related to the password change
     * @param  currentPassword The current password to check
     * @return                 true if the provided password matches with current
     *                         password
     */
    abstract public boolean canChangePassword(Context context, EPerson ePerson, String currentPassword);
}
