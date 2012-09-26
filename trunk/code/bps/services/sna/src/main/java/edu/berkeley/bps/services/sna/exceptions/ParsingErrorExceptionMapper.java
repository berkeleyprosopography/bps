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
public class ParsingErrorExceptionMapper implements ExceptionMapper<ParsingErrorException> {
	public Response toResponse(ParsingErrorException exception)
	  {
	    return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
	  }
	
}