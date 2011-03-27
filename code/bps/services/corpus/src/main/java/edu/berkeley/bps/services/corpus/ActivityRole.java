package edu.berkeley.bps.services.corpus;

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
@XmlRootElement(name="actrole")
public class ActivityRole {
	public static final String FATHER_ROLE = "Father";
	public static final String MOTHER_ROLE = "Mother";
	public static final String GRANDFATHER_ROLE = "Grandfather";
	public static final String ANCESTOR_ROLE = "Ancestor";
	public static final String CLAN_ROLE = "Clan";

	private final static String myClass = "ActivityRole";
	private static int nextId = CachedEntity.UNSET_ID_VALUE;	// temp IDs before we serialize

	@XmlElement
	private int			id;
	private Corpus		corpus;			// Each activityRole is tied to a corpus
	@XmlElement
	private String		name;
	@XmlElement
	private String		description;
	
	private boolean		familyRole;

	/**
	 * Create a new empty ActivityRole.
	 */
	public ActivityRole() {
		this(ActivityRole.nextId--, null, null, null);
	}

	/**
	 * Create a new ActivityRole with just a name.
	 * @param name A shorthand name for use in UI, etc.
	 */
	public ActivityRole( Corpus corpus, String name ) {
		this(ActivityRole.nextId--, corpus, name, null);
	}

	/**
	 * Create a new ActivityRole with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public ActivityRole( Corpus corpus, String name, String description ) {
		this(ActivityRole.nextId--, corpus, name, description);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see ActivityRole( String name, String description )
	 * @param id ID of the ActivityRole to be created. Must be unique.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public ActivityRole(int id, Corpus corpus, String name, String description) {
		this.id = id;
		this.name = name;
		this.corpus = corpus;
		this.description = description;
		familyRole = (name.equals(FATHER_ROLE)
		 || name.equals(MOTHER_ROLE)
		 || name.equals(GRANDFATHER_ROLE)
		 || name.equals(ANCESTOR_ROLE)
		 || name.equals(CLAN_ROLE));
	}

	public ActivityRole cloneInCorpus(Connection dbConn, Corpus newCorpus) {
		return CreateAndPersist(dbConn, newCorpus, name, description);
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

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}
	
	public boolean isFamilyRole() {
		return familyRole;
	}

	/**
	 * @param corpus the corpus to set
	 */
	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	public void CreateAndPersist(Connection dbConn) {
		final String myName = ".CreateAndPersist: ";
		id = persistNew(dbConn, corpus.getId(), name, description);
	}
		
	public static ActivityRole CreateAndPersist(Connection dbConn, 
			Corpus corpus, String name, String description) {
		//final String myName = ".CreateAndPersist: ";
		int newId = persistNew(dbConn,corpus.getId(), name, description);
		ActivityRole activityRole = new ActivityRole(newId, corpus, name, description); 
		return activityRole;
	}
	
	private static int persistNew(Connection dbConn, 
			int corpus_id, String name, String description) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO act_role(corpus_id, name, description, creation_time)"
			+" VALUES(?,?,?,now())";
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, corpus_id);
			stmt.setString(2, name);
			stmt.setString(3, description);
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
	
	public void persist(Connection dbConn) {
		final String myName = ".persist: ";
		// Note that we do not update the corpus_id - moving them is not allowed 
		if(id<=CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, corpus.getId(), name, description);
		} else {
			final String UPDATE_STMT = 
				"UPDATE act_role SET name=?, description=? WHERE id=?";
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
	
	public static List<ActivityRole> ListAllInCorpus(Connection dbConn, Corpus corpus) {
		// TODO Add pagination support
		// TODO rebuild parent structures from DB
		final String SELECT_ALL = 
			"SELECT id, name, description FROM act_role WHERE corpus_id=?";
		ArrayList<ActivityRole> actRoleList = new ArrayList<ActivityRole>();
		int corpus_id = corpus.getId();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			stmt.setInt(1, corpus_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				ActivityRole actRole = new ActivityRole(rs.getInt("id"), corpus, rs.getString("name"), 
						rs.getString("description"));
				actRoleList.add(actRole);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAll(): Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating activityRole\n"+se.getLocalizedMessage()).build());
		}
		return actRoleList;
	}
	
	public static boolean Exists(Connection dbConn, Corpus corpus, int id) {
		boolean exists = false;
		final String SELECT_BY_ID = 
			"SELECT name FROM act_role WHERE id = ? and corpus_id = ?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			stmt.setInt(2, corpus.getId());
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
	
	public static ActivityRole FindByID(Connection dbConn, Corpus corpus, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT id, name, description FROM act_role WHERE id = ? and corpus_id = ?";
		ActivityRole activityRole = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			stmt.setInt(2, corpus.getId());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				activityRole = new ActivityRole(rs.getInt("id"), corpus, rs.getString("name"), 
									rs.getString("description"));
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return activityRole;
	}
	
	public static ActivityRole FindByName(Connection dbConn, Corpus corpus, String name) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_NAME = 
			"SELECT id, name, description FROM act_role WHERE name = ? and corpus_id = ?";
		ActivityRole activityRole = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			stmt.setInt(2, corpus.getId());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				activityRole = new ActivityRole(rs.getInt("id"), corpus, rs.getString("name"), 
									rs.getString("description"));
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return activityRole;
	}
	
	public void deletePersistence(Connection dbConn) {
		DeletePersistence(dbConn, corpus, id);
	}
	
	public static void DeletePersistence(Connection dbConn, Corpus corpus, int id) {
		final String DELETE_STMT = "DELETE FROM act_role WHERE id=? and corpus_id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, id);
			stmt.setInt(2, corpus.getId());
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".deletePersistence: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
	}

	public static void DeleteAllInCorpus(Connection dbConn, Corpus corpus) {
		final String DELETE_ALL = 
			"DELETE FROM act_role WHERE corpus_id=?";
		int corpus_id = corpus.getId();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_ALL);
			stmt.setInt(1, corpus_id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".DeleteAllInCorpus(): Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem deleting activityRoles\n"+se.getLocalizedMessage()).build());
		}
	}
	
	/**
	 * @return Name.
	 */
	public String toString() {
		return "{"+((name==null)?"(null)":name)+"}";
	}

}
