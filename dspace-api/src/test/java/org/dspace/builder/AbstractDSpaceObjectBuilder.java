/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.builder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.DSpaceObject;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Abstract builder to construct DSpace Objects
 *
 * @author Tom Desair (tom dot desair at atmire dot com)
 * @author Raf Ponsaerts (raf dot ponsaerts at atmire dot com)
 * @param <T> concrete type of DSpaceObject
 */
public abstract class AbstractDSpaceObjectBuilder<T extends DSpaceObject>
    extends AbstractBuilder<T, DSpaceObjectService> {

    /* Log4j logger*/
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(AbstractDSpaceObjectBuilder.class);

    protected AbstractDSpaceObjectBuilder(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public abstract void cleanup() throws Exception;


    @Override
    protected abstract DSpaceObjectService<T> getService();


    @Override
    protected <B> B handleException(final Exception e) {
        log.error(e.getMessage(), e);
        return null;
    }


    protected <B extends AbstractDSpaceObjectBuilder<T>> B addMetadataValue(final T dso, final String schema,
                                                                            final String element,
                                                                            final String qualifier,
                                                                            final String value) {
        try {
            getService().addMetadata(context, dso, schema, element, qualifier, null, value);
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    protected <B extends AbstractDSpaceObjectBuilder<T>> B addMetadataValue(final T dso, final String schema,
                                                                            final String element,
                                                                            final String qualifier,
                                                                            final String language,
                                                                            final String value) {
        try {
            getService().addMetadata(context, dso, schema, element, qualifier, language, value);
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    protected <B extends AbstractDSpaceObjectBuilder<T>> B addMetadataValue(final T dso, final String schema,
                                                                            final String element,
                                                                            final String qualifier,
                                                                            final String language,
                                                                            final String value,
                                                                            final String authority,
                                                                            final int confidence) {
        try {
            getService().addMetadata(context, dso, schema, element, qualifier, language, value, authority, confidence);
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    protected <B extends AbstractDSpaceObjectBuilder<T>> B setMetadataSingleValue(final T dso, final String schema,
                                                                                  final String element,
                                                                                  final String qualifier,
                                                                                  final String value) {
        try {
            getService().setMetadataSingleValue(context, dso, schema, element, qualifier, null, value);
        } catch (Exception e) {
            return handleException(e);
        }

        return (B) this;
    }

    /**
     * Support method to grant the {@link Constants#READ} permission over an
     * object only to the {@link Group#ANONYMOUS} after the specified
     * embargoPeriod.  Any other READ permissions will be removed.
     *
     * @param <B> type of this Builder.
     * @param embargoPeriod
     *            the embargo period after which the READ permission will be
     *            active.
     * @param dso the DSpaceObject on which to grant the permission.
     * @return the builder properly configured to retain read permission on the
     *         object only for the specified group.
     */
    protected <B extends AbstractDSpaceObjectBuilder<T>> B setEmbargo(Period embargoPeriod, DSpaceObject dso) {
        // add policy just for anonymous
        try {
            LocalDate embargoDate = LocalDate.now().plus(embargoPeriod);

            return setOnlyReadPermission(dso, groupService.findByName(context, Group.ANONYMOUS), embargoDate);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Support method to grant the {@link Constants#READ} permission over an
     * object only to a specific group.Any other READ permissions will be
     * removed.
     *
     * @param <B> type of this Builder.
     * @param dso
     *            the DSpaceObject on which grant the permission
     * @param group
     *            the EPersonGroup that will be granted of the permission
     * @param startDate
     *            the date on which access begins.
     * @return the builder properly configured to retain read permission on the
     *            object only for the specified group.
     */
    protected <B extends AbstractDSpaceObjectBuilder<T>> B setOnlyReadPermission(DSpaceObject dso, Group group,
                                                                                 LocalDate startDate) {
        // add policy just for anonymous
        try {
            authorizeService.removeAllPolicies(context, dso);

            ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, group,
                                                                      null, startDate, Constants.READ,
                                                                      "Integration Test", dso);
            if (rp != null) {
                resourcePolicyService.update(context, rp);
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    /**
     * Support method to grant the {@link Constants#READ} permission over an
     * object only to a specific EPerson.  Any other READ permissions will be
     * removed.
     *
     * @param <B> type of this Builder.
     * @param dso
     *            the DSpaceObject on which grant the permission
     * @param eperson
     *            the EPerson that will be granted of the permission
     * @param startDate the date on which access begins.
     * @return the builder properly configured to build the object with the
     *            additional admin permission.
     */
    protected <B extends AbstractDSpaceObjectBuilder<T>> B setAdminPermission(DSpaceObject dso, EPerson eperson,
                                                                              LocalDate startDate) {
        try {

            ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, null,
                                                                      eperson, startDate, Constants.ADMIN,
                                                                      "Integration Test", dso);
            if (rp != null) {
                resourcePolicyService.update(context, rp);
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;

    }

    /**
     * Support method to grant {@link Constants#REMOVE} permission to a specific eperson
     *
     * @param <B> type of this Builder.
     * @param dso
     *            the DSpaceObject on which grant the permission
     * @param eperson
     *            the eperson that will be granted of the permission
     * @param startDate
     *            the optional start date from which the permission will be grant, can be <code>null</code>
     * @return the builder properly configured to build the object with the additional remove permission
     */
    protected <B extends AbstractDSpaceObjectBuilder<T>> B setRemovePermissionForEperson(DSpaceObject dso,
                                                                                         EPerson eperson,
                                                                                         LocalDate startDate) {
        try {

            ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, null,
                eperson, startDate, Constants.REMOVE,
                "Integration Test", dso);
            if (rp != null) {
                log.info("Updating resource policy with REMOVE for eperson: " + eperson.getEmail());
                resourcePolicyService.update(context, rp);
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    /**
     * Support method to grant {@link Constants#ADD} permission to a specific eperson
     *
     * @param <B> type of this Builder.
     * @param dso
     *            the DSpaceObject on which grant the permission
     * @param eperson
     *            the eperson that will be granted of the permission
     * @param startDate
     *            the optional start date from which the permission will be grant, can be <code>null</code>
     * @return the builder properly configured to build the object with the additional add permission
     */
    protected <B extends AbstractDSpaceObjectBuilder<T>> B setAddPermissionForEperson(DSpaceObject dso,
                                                                                      EPerson eperson,
                                                                                      LocalDate startDate) {
        try {

            ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, null,
                eperson, startDate, Constants.ADD,
                "Integration Test", dso);
            if (rp != null) {
                log.info("Updating resource policy with ADD for eperson: " + eperson.getEmail());
                resourcePolicyService.update(context, rp);
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    /**
     * Support method to grant {@link Constants#WRITE} permission to a specific eperson
     *
     * @param <B> type of this Builder.
     * @param dso
     *            the DSpaceObject on which grant the permission
     * @param eperson
     *            the eperson that will be granted of the permission
     * @param startDate
     *            the optional start date from which the permission will be grant, can be <code>null</code>
     * @return the builder properly configured to build the object with the additional write permission
     */
    protected <B extends AbstractDSpaceObjectBuilder<T>> B setWritePermissionForEperson(DSpaceObject dso,
                                                                                        EPerson eperson,
                                                                                        LocalDate startDate) {
        try {

            ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, null,
                eperson, startDate, Constants.WRITE,
                "Integration Test", dso);
            if (rp != null) {
                log.info("Updating resource policy with WRITE for eperson: " + eperson.getEmail());
                resourcePolicyService.update(context, rp);
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    @Override
    public abstract T build() throws SQLException, AuthorizeException;

    @Override
    public void delete(Context c, T dso) throws Exception {
        if (dso != null) {
            getService().delete(c, dso);
        }
        c.complete();
        indexingService.commit();
    }
}
