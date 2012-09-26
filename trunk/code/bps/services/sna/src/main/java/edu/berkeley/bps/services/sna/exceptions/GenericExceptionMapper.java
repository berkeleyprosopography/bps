/* Social Network Analysis Module
 * Exceptions
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.*;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
	public Response toResponse(Exception exception)
	  {
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
	    entity(exception.getMessage()).build();
	  }
	
}