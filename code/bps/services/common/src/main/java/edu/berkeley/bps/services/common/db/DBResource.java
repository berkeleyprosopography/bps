package edu.berkeley.bps.services.common.db;

import edu.berkeley.bps.services.common.BaseResource;
import edu.berkeley.bps.services.common.ServiceContext;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;


/**
 * Resource that represents DB as a trivial resource (status)
 * 
 */
@Path("/db")
public class DBResource extends BaseResource {

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
		return DBStatus.createUnavailableStatus(reason);
	}

}
