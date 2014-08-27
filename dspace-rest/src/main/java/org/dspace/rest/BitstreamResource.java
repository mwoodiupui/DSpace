/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.rest;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.rest.common.Bitstream;
import org.dspace.usage.UsageEvent;
import org.dspace.utils.DSpace;

/**
 * Created with IntelliJ IDEA.
 * User: peterdietz
 * Date: 10/2/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/bitstreams")
public class BitstreamResource {
    Logger log = Logger.getLogger(BitstreamResource.class);

    private static final boolean writeStatistics;

	static{
		writeStatistics=ConfigurationManager.getBooleanProperty("rest","stats",false);
	}

    //BitstreamList - Not Implemented

    @GET
    @Path("/{bitstream_id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Bitstream getBitstream(@PathParam("bitstream_id") Integer bitstream_id, @QueryParam("expand") String expand) {
        org.dspace.core.Context context = null;
        try {
            context = new org.dspace.core.Context();

            org.dspace.content.Bitstream bitstream = org.dspace.content.Bitstream.find(context, bitstream_id);

            if(AuthorizeManager.authorizeActionBoolean(context, bitstream, org.dspace.core.Constants.READ)) {
                return new org.dspace.rest.common.Bitstream(bitstream, expand);
            } else {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
        } catch(SQLException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            if(context != null) {
                try {
                    context.complete();
                } catch (SQLException e) {
                    log.error(e.getMessage() + " occurred while trying to close");
                }
            }
        }
    }

    /**
     * Count our bitstreams.
     *
     * @return number of bitstreams.
     */
    @GET
    @Path("/count")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Long getCount()
    {
        org.dspace.core.Context context;
        long count;
        try {
            context = new org.dspace.core.Context();
            count = org.dspace.content.Bitstream.count(context);
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return count;
    }

    /**
     * Sum sizes of matching bitstreams.
     *
     * @param unit omitted; or KB, MB, GB, TB, PB, EB, ZB, YB.
     * @param mimeType omitted; or specific (text/plain); or general (image/*).
     *                  Not a regular expression or a shell-style wildcard.
     * @return
     */
    @GET
    @Path("/total")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public BigDecimal getTotal(@QueryParam("unit") String unit,
            @QueryParam("mimeType") String mimeType)
    {
        org.dspace.core.Context context;
        BigDecimal total;
        try {
            context = new org.dspace.core.Context();
            // TODO MIME type match
            total = org.dspace.content.Bitstream.total(context);
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        // scale it
        int scaleFactor;
        switch (unit) {
        case "KB": scaleFactor = 3; break;
        case "MB": scaleFactor = 6; break;
        case "GB": scaleFactor = 9; break;
        case "TB": scaleFactor = 12; break;
        case "PB": scaleFactor = 15; break;
        case "EB": scaleFactor = 18; break;
        case "ZB": scaleFactor = 21; break;
        case "YB": scaleFactor = 24; break;
        default: scaleFactor = 1;
        }

        return total.movePointLeft(scaleFactor);
    }

    @GET
    @Path("/{bitstream_id}/retrieve")
    public javax.ws.rs.core.Response getFile(@PathParam("bitstream_id") final Integer bitstream_id,
    		@QueryParam("userIP") String user_ip, @QueryParam("userAgent") String user_agent, @QueryParam("xforwarderfor") String xforwarderfor,
    		@Context HttpHeaders headers, @Context HttpServletRequest request) {
        org.dspace.core.Context context = null;
        try {
            context = new org.dspace.core.Context();

            org.dspace.content.Bitstream bitstream = org.dspace.content.Bitstream.find(context, bitstream_id);
            if(AuthorizeManager.authorizeActionBoolean(context, bitstream, org.dspace.core.Constants.READ)) {
            	if(writeStatistics){
    				writeStats(context, bitstream_id, user_ip, user_agent, xforwarderfor, headers, request);
    			}

                return Response.ok(bitstream.retrieve()).type(bitstream.getFormat().getMIMEType()).build();
            } else {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (AuthorizeException e) {
            log.error(e.getMessage());
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            if(context != null) {
                try {
                    context.complete();
                } catch (SQLException e) {
                    log.error(e.getMessage() + " occurred while trying to close");
                }
            }
        }
    }

	private void writeStats(org.dspace.core.Context context, Integer bitstream_id, String user_ip, String user_agent,
			String xforwarderfor, HttpHeaders headers,
			HttpServletRequest request) {

    	try{
    		DSpaceObject bitstream = DSpaceObject.find(context, Constants.BITSTREAM, bitstream_id);

    		if(user_ip==null || user_ip.length()==0){
    			new DSpace().getEventService().fireEvent(
	                     new UsageEvent(
	                                     UsageEvent.Action.VIEW,
	                                     request,
	                                     context,
	                                     bitstream));
    		} else{
	    		new DSpace().getEventService().fireEvent(
	                     new UsageEvent(
	                                     UsageEvent.Action.VIEW,
	                                     user_ip,
	                                     user_agent,
	                                     xforwarderfor,
	                                     context,
	                                     bitstream));
    		}
    		log.debug("fired event");

		} catch(SQLException ex){
			log.error("SQL exception can't write usageEvent \n" + ex);
		}

	}

}
