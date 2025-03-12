/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.access.status;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;

import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.access.status.factory.AccessStatusServiceFactory;
import org.dspace.access.status.service.AccessStatusService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.InstallItemService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Tests for access status service
 */
public class AccessStatusServiceTest extends AbstractUnitTest {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(AccessStatusServiceTest.class);

    private Collection collection;
    private Community owningCommunity;
    private Item item;
    private Bundle bundle;
    private Bitstream bitstream;

    protected CommunityService communityService =
            ContentServiceFactory.getInstance().getCommunityService();
    protected CollectionService collectionService =
            ContentServiceFactory.getInstance().getCollectionService();
    protected ItemService itemService =
            ContentServiceFactory.getInstance().getItemService();
    protected BundleService bundleService =
            ContentServiceFactory.getInstance().getBundleService();
    protected BitstreamService bitstreamService =
            ContentServiceFactory.getInstance().getBitstreamService();
    protected WorkspaceItemService workspaceItemService =
            ContentServiceFactory.getInstance().getWorkspaceItemService();
    protected InstallItemService installItemService =
            ContentServiceFactory.getInstance().getInstallItemService();
    protected AccessStatusService accessStatusService =
            AccessStatusServiceFactory.getInstance().getAccessStatusService();

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init() {
        super.init();
        try {
            context.turnOffAuthorisationSystem();
            owningCommunity = communityService.create(null, context);
            collection = collectionService.create(context, owningCommunity);
            item = installItemService.installItem(context,
                    workspaceItemService.create(context, collection, true));
            bundle = bundleService.create(context, item, Constants.CONTENT_BUNDLE_NAME);
            bitstream = bitstreamService.create(context, bundle,
                new ByteArrayInputStream("1".getBytes(StandardCharsets.UTF_8)));
            bitstream.setName(context, "primary");
            context.restoreAuthSystemState();
        } catch (AuthorizeException ex) {
            log.error("Authorization Error in init", ex);
            fail("Authorization Error in init: " + ex.getMessage());
        } catch (SQLException ex) {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
        } catch (IOException ex) {
            log.error("IO Error in init", ex);
            fail("IO Error in init: " + ex.getMessage());
        }
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy() {
        context.turnOffAuthorisationSystem();
        try {
            bitstreamService.delete(context, bitstream);
        } catch (Exception e) {
            // ignore
        }
        try {
            bundleService.delete(context, bundle);
        } catch (Exception e) {
            // ignore
        }
        try {
            itemService.delete(context, item);
        } catch (Exception e) {
            // ignore
        }
        try {
            collectionService.delete(context, collection);
        } catch (Exception e) {
            // ignore
        }
        try {
            communityService.delete(context, owningCommunity);
        } catch (Exception e) {
            // ignore
        }
        context.restoreAuthSystemState();
        bitstream = null;
        bundle = null;
        item = null;
        collection = null;
        owningCommunity = null;
        try {
            super.destroy();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testGetAccessStatus() throws Exception {
        String status = accessStatusService.getAccessStatus(context, item);
        assertNotEquals("testGetAccessStatus 0", status, DefaultAccessStatusHelper.UNKNOWN);
    }

    @Test
    public void testGetEmbargoFromItem() throws Exception {
        String embargo = accessStatusService.getEmbargoFromItem(context, item);
        assertNull("testGetEmbargoFromItem 0", embargo);
    }

    @Test
    public void testGetAvailabilityDateFromBitstream() throws Exception {
        LocalDate availabilityDate = accessStatusService.getAvailabilityDateFromBitstream(context, bitstream);
        assertNull("testGetAvailabilityDateFromBitstream 0", availabilityDate);
    }

    @Test
    public void testGetAccessStatusFromAvailabilityDate() throws Exception {
        LocalDate availabilityDate = accessStatusService.getAvailabilityDateFromBitstream(context, bitstream);
        String status = accessStatusService.getAccessStatusFromAvailabilityDate(availabilityDate);
        assertNotEquals("testGetAccessStatusFromAvailabilityDate 0", status, DefaultAccessStatusHelper.UNKNOWN);
    }
}
