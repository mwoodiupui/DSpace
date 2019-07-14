/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.DecoderException;
import org.dspace.AbstractDSpaceTest;
import org.dspace.core.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mwood
 */
public class PasswordHashTest extends AbstractDSpaceTest {
    public PasswordHashTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test the constructors.
     * @throws DecoderException passed through.
     */
    @Test
    public void testConstructors()
        throws DecoderException {
        SimplePasswordHash h1;
        SimplePasswordHash h3;

        // Test null inputs, as from NULL database columns (old EPerson using
        // unsalted hash, for example).
        h3 = new SimplePasswordHash(null, (byte[]) null, (byte[]) null);
        assertNull("Null algorithm", h3.getHashAlgorithm());
        assertNull("Null salt", h3.getSalt());
        assertNull("Null hash", h3.getHash());
        assertFalse("Match null string?", h3.equals(null));
        assertFalse("Match non-null string?", h3.equals("not null"));

        // Test 3-argument constructor with null string arguments
        h3 = new SimplePasswordHash(null, (String) null, (String) null);
        assertNull("Null algorithm", h3.getHashAlgorithm());
        assertNull("Null salt", h3.getSalt());
        assertNull("Null hash", h3.getHash());
        assertFalse("Match null string?", h3.equals(null));
        assertFalse("Match non-null string?", h3.equals("not null"));

        // Test single-argument constructor, which does the hashing.
        String password = "I've got a secret.";
        h1 = new SimplePasswordHash(password);
        assertEquals("SHA-512", h1.getHashAlgorithm());
        assertFalse("Match against a different string", h1.equals("random rubbish"));
        assertTrue("Match against the correct string", h1.equals(password));

        // Test 3-argument constructor with non-null data.
        h3 = new SimplePasswordHash(h1.getHashAlgorithm(), h1.getSalt(), h1.getHash());
        assertTrue("Match a duplicate original made from getter values", h3.equals(password));
    }

    /**
     * Test of equals method, of class SimplePasswordHash.
     * @throws NoSuchAlgorithmException passed through.
     * @throws UnsupportedEncodingException passed through.
     */
    @Test
    public void testEquals()
        throws NoSuchAlgorithmException, UnsupportedEncodingException {
        System.out.println("matches");
        final String secret = "Clark Kent is Superman";

        // Test old 1-trip MD5 hash
        MessageDigest digest = MessageDigest.getInstance("MD5");
        SimplePasswordHash hash = new SimplePasswordHash(null, null,
                digest.digest(secret.getBytes(Constants.DEFAULT_ENCODING)));
        boolean result = hash.equals(secret);
        assertTrue("Old unsalted 1-trip MD5 hash", result);

        // 3-argument form:  see constructor tests
    }

    /**
     * Test of getHash method, of class PasswordHash.
     */
    /*
    @Test
    public void testGetHash()
    {
        System.out.println("getHash");
        SimplePasswordHash instance = null;
        byte[] expResult = null;
        byte[] result = instance.getHash();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of getSalt method, of class PasswordHash.
     */
    /*
    @Test
    public void testGetSalt()
    {
        System.out.println("getSalt");
        SimplePasswordHash instance = null;
        byte[] expResult = null;
        byte[] result = instance.getSalt();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of getAlgorithm method, of class PasswordHash.
     */
    /*
    @Test
    public void testGetAlgorithm()
    {
        System.out.println("getAlgorithm");
        SimplePasswordHash instance = null;
        String expResult = "";
        String result = instance.getAlgorithm();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
