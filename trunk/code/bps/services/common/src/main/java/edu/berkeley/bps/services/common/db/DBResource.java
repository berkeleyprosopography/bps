package edu.berkeley.bps.services.common.db;

import edu.berkeley.bps.services.common.BaseResource;
import edu.berkeley.bps.services.common.ServiceContext;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Resource that represents DB as a trivial resource (status)
 * 
 */
@Path("/db")
public class DBResource extends BaseResource {

	final Logger logger = LoggerFactory.getLogger(DBResource.class);
	
	private static final String myClass = "DBResource";

	@GET
	@Produces({"application/xml", "application/json"})
	public DBStatus get(@Context ServletContext srvc) {
		String reason = "Unknown";
		try {
			ServiceContext sc = getServiceContext(srvc);
			if(sc.isAvailable())
				return DBStatus.createAvailableStatus();
			reason = "System is under maintenance.";
		} catch (RuntimeException rte) {
			// Quietly absorb DB troubles
			reason = rte.getMessage();
		}
		logger.warn("System unavailable: {}", reason);
		return DBStatus.createUnavailableStatus(reason);
	}

}
