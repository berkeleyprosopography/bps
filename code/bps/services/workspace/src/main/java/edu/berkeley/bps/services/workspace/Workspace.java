package edu.berkeley.bps.services.workspace;

import edu.berkeley.bps.services.corpus.CachedEntity;
import edu.berkeley.bps.services.corpus.Corpus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="workspace")
public class Workspace {
	private final static String myClass = "Workspace";
	private static int nextId = CachedEntity.UNSET_ID_VALUE;	// temp IDs before we serialize

	@XmlElement
	private int			id;
	@XmlElement
	private String		name;
	@XmlElement
	private String		description;
	@XmlElement
	private int			owner_id;			// Each workspace is tied to a user
	
	private Corpus		corpus;

	public Workspace() {
		this(Workspace.nextId--,null,null,0);
	}

	/**
	 * @param id
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 * @param owner_id The user that owns this workspace
	 */
	private Workspace(int id, String name, String description, int owner_id) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.owner_id = owner_id;
		System.err.println("Workspace.ctor, created: "+this.toString());
	}

	public void CreateAndPersist(Connection dbConn) {
		//final String myName = ".CreateAndPersist: ";
		id = persistNew(dbConn, name, description, owner_id);
	}
		
	public static Workspace CreateAndPersist(Connection dbConn, 
			String name, String description, int owner_id) {
		//final String myName = ".CreateAndPersist: ";
		int newId = persistNew(dbConn,name, description, owner_id);
		Workspace workspace = new Workspace(newId, name, description, owner_id); 
		return workspace;
	}
	
	private static int persistNew(Connection dbConn, 
			String name, String description, int owner_id) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO workspace(name, description, owner_id, creation_time)"
			+" VALUES(?,?,?,now())";
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, owner_id);
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
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return newId;
	}
	
	public void persist(Connection dbConn, boolean shallow) {
		final String myName = ".persist: ";
		if(id<=CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, name, description, owner_id);
		} else {
			// Note that we do not update the owner_id - moving them is not allowed 
			final String UPDATE_STMT = 
				"UPDATE workspace SET name=?, description=? WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setString(1, name);
				stmt.setString(2, description);
				stmt.setInt(3, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				System.err.println(tmp);
				throw new RuntimeException( tmp );
			}
		}
	}
	
	public static List<Workspace> ListAllForUser(Connection dbConn, int user_id) {
		// TODO Add pagination support
		final String SELECT_ALL = 
			"SELECT id, name, description FROM workspace WHERE owner_id=?";
		ArrayList<Workspace> wkspList = new ArrayList<Workspace>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			stmt.setInt(1, user_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Workspace workspace = new Workspace(rs.getInt("id"), 
						rs.getString("name"), 
						rs.getString("description"),
						user_id);
				wkspList.add(workspace);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllForUser(): Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating workspace\n"+se.getLocalizedMessage()).build());
		}
		return wkspList;
	}
	
	public static boolean Exists(Connection dbConn, int id) {
		boolean exists = false;
		final String SELECT_BY_ID = 
			"SELECT name FROM workspace WHERE id = ?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				if(rs.getString("name")!=null)
					exists = true;
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			// Just absorb it
			String tmp = myClass+".Exists: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
		}
		return exists;
	}

	public static Workspace FindByID(Connection dbConn, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT id, name, description, owner_id FROM workspace WHERE id = ?";
		Workspace workspace = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				workspace = new Workspace(
						rs.getInt("id"), 
						rs.getString("name"), 
						rs.getString("description"), 
						rs.getInt("owner_id"));
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return workspace;
	}
	
	public static Workspace FindByName(Connection dbConn, int user_id, String name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT id, name, description FROM workspace WHERE name = ? and owner_id = ?";
		Workspace workspace = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			stmt.setInt(2, user_id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				workspace = new Workspace(rs.getInt("id"), rs.getString("name"), 
									rs.getString("description"), user_id); 
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return workspace;
	}
	
	public void deletePersistence(Connection dbConn) {
		DeletePersistence(dbConn, id);
	}
	
	public static void DeletePersistence(Connection dbConn, int id) {
		final String DELETE_STMT = "DELETE FROM workspace WHERE id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".deletePersistence: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
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
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public Corpus getCorpus() {
		return corpus;
	}

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}
	
	@XmlElement(name="importedCorpus")
	public String getImportedCorpus() {
		if(corpus!=null) {
			return corpus.getName();
		} else {
			return null;
		}
	}

	@XmlElement(name="medianDocDate")
	public String getMedianDocumentDate() {
		if(corpus!=null) {
			return corpus.getMedianDocumentDate();
		} else {
			return null;
		}
	}

	/**
	 * @return Name.
	 */
	public String toString() {
		return "{id:"+id
		+", name:"+((name==null)?"(null)":name)
		+"}";
	}

}
