/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.rest;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * "Smoke test":  does the /test endpoint respond?
 *
 * @author mwood
 */
public class SmokeIT
{
    private static final String smokeResponse = "REST api is running.";

    private static String baseURL;

    @BeforeClass
    public static void setup()
    {
        baseURL = System.getProperty("dspace.testing.baseURL", "http://localhost"); // TODO more reliable URL composition
    }

    @Test
    public void smokeTest()
    {
        try {
            final WebConversation webConversation = new WebConversation();
            final WebRequest webRequest = new GetMethodWebRequest(baseURL + "/test");
            WebResponse webResponse = webConversation.getResponse(webRequest);
            Assert.assertEquals(smokeResponse, webResponse.getText());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
