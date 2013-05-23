/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle;

import java.sql.SQLException;
import java.util.Enumeration;
import mockit.UsingMocksAndStubs;
import org.dspace.AbstractUnitTest;
import org.dspace.MockConfigurationManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.content.Site;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.MockDatabaseManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the HandlePlugin.
 * 
 * Note that it is not possible to properly test HandlePlugin at this time,
 * because the namespace is not set up properly.  I've written this to test what
 * we actually have.  This is mostly just a test driver for DS-357 but should be
 * reworked into a proper test suite someday.
 *
 * @author Mark H. Wood
 */
@UsingMocksAndStubs(value={MockDatabaseManager.class, MockConfigurationManager.class})
public class HandlePluginTest
    extends AbstractUnitTest
{
    private static final int[] indexList = null; // Ignored
    private static final byte[][] typeList = null; // Ignored
    private static final String HANDLE_PREFIX = "12345.test";
    private static final String TEST_HANDLE_1 = "6789";
    private static final String TEST_NA_HANDLE = "0.NA/" + HANDLE_PREFIX;

    private static Community testCommunity;

    public HandlePluginTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws SQLException, AuthorizeException
    {
        // Configure the Handle subsystem
        MockConfigurationManager.setProperty("handle.prefix", HANDLE_PREFIX);
        MockConfigurationManager.setProperty("dspace.url", "http://dspace.example.com/xmlui");

        Context ctx = new Context();

        // Create a Name Authority handle
        HandleManager.createHandle(ctx, Site.find(ctx, 0), TEST_NA_HANDLE);

        // Create an object to have a handle that we can work with
        ctx.turnOffAuthorisationSystem();
        testCommunity = Community.create(null, ctx, TEST_HANDLE_1);
        ctx.restoreAuthSystemState();

        ctx.complete();
    }

    @AfterClass
    public static void tearDownClass()
    {
        try {
            testCommunity.delete();
            // XXX There is no way to delete handles.
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

/*
    @Before
    public void setUp()
    {
    }
*/
 
/*
    @After
    public void tearDown()
    {
    }
*/

    /**
     * Test of init method, of class HandlePlugin.
     */
/*
    @Test
    public void testInit()
            throws Exception
    {
        System.out.println("init");
        StreamTable st = null;
        HandlePlugin instance = new HandlePlugin();
        instance.init(st);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of setHaveNA method, of class HandlePlugin.
     */
/*
    @Test
    public void testSetHaveNA()
            throws Exception
    {
        System.out.println("setHaveNA");
        byte[] theHandle = null;
        boolean haveit = false;
        HandlePlugin instance = new HandlePlugin();
        instance.setHaveNA(theHandle, haveit);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of createHandle method, of class HandlePlugin.
     */
/*
    @Test
    public void testCreateHandle()
            throws Exception
    {
        System.out.println("createHandle");
        byte[] theHandle = null;
        HandleValue[] values = null;
        HandlePlugin instance = new HandlePlugin();
        instance.createHandle(theHandle, values);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of deleteHandle method, of class HandlePlugin.
     */
/*
    @Test
    public void testDeleteHandle()
            throws Exception
    {
        System.out.println("deleteHandle");
        byte[] theHandle = null;
        HandlePlugin instance = new HandlePlugin();
        boolean expResult = false;
        boolean result = instance.deleteHandle(theHandle);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of updateValue method, of class HandlePlugin.
     */
/*
    @Test
    public void testUpdateValue()
            throws Exception
    {
        System.out.println("updateValue");
        byte[] theHandle = null;
        HandleValue[] values = null;
        HandlePlugin instance = new HandlePlugin();
        instance.updateValue(theHandle, values);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of deleteAllRecords method, of class HandlePlugin.
     */
/*
    @Test
    public void testDeleteAllRecords()
            throws Exception
    {
        System.out.println("deleteAllRecords");
        HandlePlugin instance = new HandlePlugin();
        instance.deleteAllRecords();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of checkpointDatabase method, of class HandlePlugin.
     */
/*
    @Test
    public void testCheckpointDatabase()
            throws Exception
    {
        System.out.println("checkpointDatabase");
        HandlePlugin instance = new HandlePlugin();
        instance.checkpointDatabase();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of shutdown method, of class HandlePlugin.
     */
/*
    @Test
    public void testShutdown()
    {
        System.out.println("shutdown");
        HandlePlugin instance = new HandlePlugin();
        instance.shutdown();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of scanHandles method, of class HandlePlugin.
     */
/*
    @Test
    public void testScanHandles()
            throws Exception
    {
        System.out.println("scanHandles");
        ScanCallback callback = null;
        HandlePlugin instance = new HandlePlugin();
        instance.scanHandles(callback);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of scanNAs method, of class HandlePlugin.
     */
/*
    @Test
    public void testScanNAs()
            throws Exception
    {
        System.out.println("scanNAs");
        ScanCallback callback = null;
        HandlePlugin instance = new HandlePlugin();
        instance.scanNAs(callback);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of getRawHandleValues method, of class HandlePlugin.
     */
    @Test
    public void testGetRawHandleValues()
            throws Exception
    {
        System.out.println("getRawHandleValues");

        HandlePlugin instance = new HandlePlugin();

        byte[] theHandle = TEST_HANDLE_1.getBytes();
        byte[][] result = instance.getRawHandleValues(theHandle, indexList,
                typeList);
        assertNotNull(result); // TODO did we get a reasonable value?
    }

    /**
     * Test of haveNA method, of class HandlePlugin.
     */
    /* */
    @Test
    public void testHaveNA()
            throws Exception
    {
        System.out.println("haveNA");

        HandlePlugin instance = new HandlePlugin();
        boolean result = instance.haveNA(TEST_NA_HANDLE.getBytes());
        assertTrue("returned false", result);
    }
/**/

    /**
     * Test of getHandlesForNA method, of class HandlePlugin.
     */
    @Test
    public void testGetHandlesForNA()
            throws Exception
    {
        System.out.println("getHandlesForNA");
        byte[] theNAHandle = (TEST_NA_HANDLE).getBytes();
        HandlePlugin instance = new HandlePlugin();
        Enumeration result = instance.getHandlesForNA(theNAHandle);
        assertNotNull(result);
        assertTrue(result.hasMoreElements()); // TODO did we get a reasonable value?
    }
}
