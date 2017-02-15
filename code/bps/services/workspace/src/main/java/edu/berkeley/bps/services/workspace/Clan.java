package edu.berkeley.bps.services.workspace;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;

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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="clan")
public class Clan extends Entity {

	final static Logger logger = LoggerFactory.getLogger(Clan.class);
	
	private final static String myClass = "Clan";
	// We keep track of this, the DB will do +1 automatically 
	private static int nextId = CachedEntity.UNSET_ID_VALUE;	// temp IDs before we serialize

	@XmlElement
	private int			id;
	
	// workspace_id comes from Entity
	
	@XmlElement
	Name		name;
	
	private NameRoleActivity nrad;
	
	// name should not be in the database but resolved thorugh the NRAD
	// name_id also not in database as retriveved via a JOIN of NRAD and NAME tables
	
	// Things that we need to persist because we're passing them in and out 
	@XmlElement
	private int			name_id;
	
	@XmlElement
	private int			nrad_id;
	
	
	// Constructor to create a clan object starting from an NRAD
	public Clan(NameRoleActivity nrad) {
		super( nrad );
		this.nrad = nrad;
		nrad_id = nrad.getId();
		name_id = nrad.getNameId();
		name = nrad.getName();
	}

	// Constructor to create a clan object starting from an ID
	public Clan(int target_id) {
		// Load from database
		
	}
	
	// Constructor to create a clan object starting from an ID and NRAD ID
	public Clan(int id, int workspace_id, int nrad_id, int name_id) {
		
	}
	
	protected Clan() {
		// Entity superclass Ctor with no args will throw a RuntimeException (MUST have an NRAD)
		//throw new RuntimeException("No-arg Ctor should not be called");
	}

	// This is an object-level construct only
	public void CreateAndPersist(Connection dbConn) {
		final String myName = ".CreateAndPersist: ";
		// this is the call that actually does things
		id = persistNew(dbConn, workspace_id,  nrad_id);
	}
	
	// this is a factory
	public static Clan CreateAndPersist(Connection dbConn, 
			int workspace_id,
			int nrad_id,
			int name_id) {
		return CreateAndPersist(dbConn, workspace_id, nrad_id, name_id);
	}
	

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
				"UPDATE clan SET workspace_id=?, nrad_id=?  WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setInt(1, workspace_id);
				stmt.setInt(2, nrad_id);
				stmt.setInt(3, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				logger.error(tmp);
				throw new RuntimeException( tmp );
			}
		}
	}
	
	public static List<Clan> ListAllInWorkspace(Connection dbConn, int workspace_id) {
		// TODO Add pagination support
		// TODO rebuild parent structures from DB
		final String SELECT_ALL = 
			"SELECT id FROM clan WHERE workspace_id=?";
		ArrayList<Clan> clanList = new ArrayList<Clan>();

		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			stmt.setInt(1, workspace_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Clan clan = new Clan(rs.getInt("id"));
				clanList.add(clan);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAll(): Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating activity\n"+se.getLocalizedMessage()).build());
		}
		return clanList;
	}
	
	public static void DeleteAllInWorkspace(Connection dbConn, int workspace_id) {
		final String DELETE_ALL = 
			"DELETE FROM clan WHERE workspace_id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_ALL);
			stmt.setInt(1, workspace_id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".DeleteAllInWorkspacei(): Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem deleting activities\n"+se.getLocalizedMessage()).build());
		}
	}
	
	public static boolean Exists(Connection dbConn, int workspace_id, int id) {
		boolean exists = false;
		final String SELECT_BY_ID = 
			"SELECT nrad_id FROM clan WHERE id = ? and workspace_id = ?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			stmt.setInt(2, workspace_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				if(rs.getString("nrad_id")!=null)
					exists = true;
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			// Just absorb it
			String tmp = myClass+".Exists: Problem querying DB.\n"+ se.getMessage();
			logger.debug(tmp);
		}
		return exists;
	}

	
	public static Clan FindByID(Connection dbConn, int workspace_id, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT name_id, nrad_id FROM clan WHERE id = ? and workspace_id = ?";
		Clan clan= null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			stmt.setInt(2, workspace_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				clan = new Clan(rs.getInt("id"), workspace_id, rs.getInt("nrad_id"), 
						rs.getInt("name_id"));
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		return clan;
	}
	
	
	
	public Clan FindByName(Connection dbConn, Name name){
		Clan clan = FindByName(dbConn, workspace_id, name);
		return clan;
	}
	
	public static Clan FindByName(Connection dbConn, int workspace_id, Name name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT id, nrad_id FROM clan WHERE name_id = ? and workspace_id = ?";
		Clan clan = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setInt(1, name.getId());
			stmt.setInt(2, workspace_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				clan = new Clan(rs.getInt("id"), workspace_id, rs.getInt("nrad_id"), 
						rs.getInt("name_id")); 
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		return clan;
	}
 
	public void deletePersistence(Connection dbConn) {
		DeletePersistence(dbConn, workspace_id, id);
	}
	
	public static void DeletePersistence(Connection dbConn, int workspace_id, int id) {
		final String DELETE_STMT = "DELETE FROM clan WHERE id=? and workspace_id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, id);
			stmt.setInt(2, workspace_id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".deletePersistence: Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	
	/**
	 * @return the name
	 */
	public String getName() {
		if(name != null){
			return name.getName();
		}
		else{
			//name = getNameFromNrad(id);
			return name.getName();
		}
	}

	@Override
	public int getNumQualifiers() {
		// Clans are not (yet) qualified in any way.
		return 0;
	}
	
	public static class DisplayNameComparator implements Comparator<Clan> {
		public int compare(Clan clan1, Clan clan2) {
			return clan1.displayName.compareTo(clan2.displayName);
		}
	}
	
}
