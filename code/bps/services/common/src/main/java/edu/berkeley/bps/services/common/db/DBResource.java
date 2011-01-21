package edu.berkeley.bps.services.common.db;

import edu.berkeley.bps.services.common.BaseResource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


/**
 * Resource that represents DB as a trivial resource (status)
 * 
 */
@Path("/db")
public class DBResource extends BaseResource {

	private static final String myClass = "DBResource";

	private DBStatus getStatus() {
		Connection dbConn = null;
		DBStatus dbs = null;
		try {
			dbConn = openConnection(true);
			closeConnection(dbConn);
			dbs = DBStatus.createAvailableStatus();
		} catch (RuntimeException rte) {
			dbs = DBStatus.createUnavailableStatus(rte.getMessage());
		}
		return dbs;
	}

	@GET
	@Produces("application/xml")
	public DBStatus getXML() {
		return getStatus();
	}

 	@GET
	@Produces("application/json")
	public DBStatus getJSON() {
		return getStatus();
	}
 
}
