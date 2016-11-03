package edu.berkeley.bps.services.workspace;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.berkeley.bps.services.corpus.*;
import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.common.time.*;

/**
 * @author dsem
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="clan")
public class Clan extends Entity {
	final static Logger logger = LoggerFactory.getLogger(Clan.class);
	
	private final static String myClass = "Clan";
	// We keep track of this, the DB will do +1 automatically 
	private static int nextId = CachedEntity.UNSET_ID_VALUE;	// temp IDs before we serialize
	
	// Things that we need to persist because we're passing them in and out 
	@XmlElement
	private int			id;
	
	@XmlElement
	private String		name;
	
	// name should not be in the database but resolved thorugh the NRAD
	// name_id also not in database as retriveved via a JOIN of NRAD and NAME tables
	
	@XmlElement
	private int			name_id;
	
	@XmlElement
	private int			nrad_id;
	@XmlElement
	private int			workspace_id;


	// public constructor, should never be called
	public Clan(NameRoleActivity nrad) {
		super( nrad );
	}

	@Override
	public int getNumQualifiers() {
		// Clans are not (yet) qualified in any way.
		return 0;
	}

	// This is an object-level construct only
	public void CreateAndPersist(Connection dbConn) {
		final String myName = ".CreateAndPersist: ";
		// this is the call that actually does things
		id = persistNew(dbConn, workspace_id,  nrad_id);
	}
	
	// this is a factory
	public static Activity CreateAndPersist(Connection dbConn, 
			int workspace_id, String name, int nrad_id,
			int name_id) {
		return CreateAndPersist(dbConn, workspace_id, name, nrad_id, name_id);
	}

	
	// This is it, the big one
	private static int persistNew(Connection dbConn,
			int workspace_id,
			int nrad_id) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO clan(workspace_id, nrad_id, creation_time)"
			+" VALUES(?,?,?,?,now())";
		int newId = 0;
		try {
			// prepare the statement
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
					Statement.RETURN_GENERATED_KEYS);
			
			// prevent SQL inj (escapes + type safety)
			stmt.setInt(1, workspace_id);
			stmt.setInt(2, nrad_id);
			int nRows = stmt.executeUpdate();
			if(nRows==1){
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next()){
					newId = rs.getInt(1); 
				}
				rs.close();
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return newId;
	}
	
	public void persist(Connection dbConn) {
		final String myName = ".persist: ";
		// this should never be called, only here for safety
		if(id<=CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, workspace_id, nrad_id);
		} else {
			// Note that we do not update the corpus_id - moving them is not allowed 
			final String UPDATE_STMT = 
				"UPDATE clan SET name=?, description=?, parent_id=? WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setInt(1, workspace_id);
				stmt.setInt(2, nrad_id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				logger.error(tmp);
				throw new RuntimeException( tmp );
			}
		}
	}
	
	
}
