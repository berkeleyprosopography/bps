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

import edu.berkeley.bps.services.common.time.TimeSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="activity")
public class Activity {
	final static Logger logger = LoggerFactory.getLogger(Activity.class);
			
	private final static String myClass = "Activity";
	private static int nextId = CachedEntity.UNSET_ID_VALUE;	// temp IDs before we serialize

	@XmlElement
	private int			id;
	private Corpus		corpus;			// Each activity is tied to a corpus
	@XmlElement
	private String		name;
	@XmlElement
	private String		description;
	// Not fully supported yet
	private Activity	parent;
	private ArrayList<Activity>	children;

	public Activity() {
		this(Activity.nextId--,null,null,null,null);
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @param corpus the corpus to set
	 */
	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	/**
	 * @param id
	 * @param corpus The owning corpus for this name
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 * @param parent Broader activity that this specializes.
	 */
	private Activity(int id, Corpus corpus, String name, String description, Activity parent) {
		this.id = id;
		this.name = name;
		this.corpus = corpus;
		this.description = description;
		this.parent = parent;
		if(parent!=null)
			parent.addChild(this);
		logger.trace("Activity.ctor, created: {}", this.toString());
	}
	
	/**
	 * @see Activity(int id, Corpus corpus, String name, String description, Activity parent)
	 * @param id
	 * @param corpus The owning corpus for this name
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Activity(int id, Corpus corpus, String name, String description) {
		this(id, corpus, name, description, null);
	}

	public Activity cloneInCorpus(Connection dbConn, Corpus newCorpus) {
		final String myName = ".cloneInCorpus: ";
		if(parent!=null) {
			String tmp = myClass+myName+"Cannot clone Activity with parent (NYI).\n";
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		return CreateAndPersist(dbConn, newCorpus, name, description, null);
	}

	/**
	 * @see Activity(int id, Corpus corpus, String name, String description, Activity parent)
	 * @param corpus The owning corpus for this name
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Activity(Corpus corpus, String name) {
		this(Activity.nextId--, corpus, name, null, null);
	}

	public void CreateAndPersist(Connection dbConn) {
		final String myName = ".CreateAndPersist: ";
		id = persistNew(dbConn, corpus.getId(), name, description, parent);
	}
		
	public static Activity CreateAndPersist(Connection dbConn, 
			Corpus corpus, String name, String description) {
		return CreateAndPersist(dbConn, corpus, name, description, null);
	}
	
	private static Activity CreateAndPersist(Connection dbConn, 
			Corpus corpus, String name, String description, Activity parent) {
		//final String myName = ".CreateAndPersist: ";
		int newId = persistNew(dbConn,corpus.getId(), name, description, parent);
		Activity activity = new Activity(newId, corpus, name, description, parent); 
		return activity;
	}
	
	private static int persistNew(Connection dbConn, 
			int corpus_id, String name, String description, Activity parent) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO activity(corpus_id, name, description, parent_id, creation_time)"
			+" VALUES(?,?,?,?,now())";
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, corpus_id);
			stmt.setString(2, name);
			stmt.setString(3, description);
			if(parent==null) {
				stmt.setNull(4, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(4, parent.getId());
			}
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
		if(id<=CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, corpus.getId(), name, description, parent);
		} else {
			// Note that we do not update the corpus_id - moving them is not allowed 
			final String UPDATE_STMT = 
				"UPDATE activity SET name=?, description=?, parent_id=? WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setString(1, name);
				stmt.setString(2, description);
				if(parent==null) {
					stmt.setNull(3, java.sql.Types.INTEGER);
				} else {
					stmt.setInt(3, parent.getId());
				}
				stmt.setInt(4, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				logger.error(tmp);
				throw new RuntimeException( tmp );
			}
		}
	}
	
	public static List<Activity> ListAllInCorpus(Connection dbConn, Corpus corpus) {
		// TODO Add pagination support
		// TODO rebuild parent structures from DB
		final String SELECT_ALL = 
			"SELECT id, name, description FROM activity WHERE corpus_id=?";
		ArrayList<Activity> activityList = new ArrayList<Activity>();
		int corpus_id = corpus.getId();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			stmt.setInt(1, corpus_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Activity activity = new Activity(rs.getInt("id"), corpus, rs.getString("name"), 
						rs.getString("description"));
				activityList.add(activity);
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
		return activityList;
	}
	
	public static void DeleteAllInCorpus(Connection dbConn, Corpus corpus) {
		final String DELETE_ALL = 
			"DELETE FROM activity WHERE corpus_id=?";
		int corpus_id = corpus.getId();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_ALL);
			stmt.setInt(1, corpus_id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".DeleteAllInCorpus(): Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem deleting activities\n"+se.getLocalizedMessage()).build());
		}
	}
	
	public static boolean Exists(Connection dbConn, Corpus corpus, int id) {
		boolean exists = false;
		final String SELECT_BY_ID = 
			"SELECT name FROM activity WHERE id = ? and corpus_id = ?";
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
			logger.debug(tmp);
		}
		return exists;
	}

	public static Activity FindByID(Connection dbConn, Corpus corpus, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT id, name, description FROM activity WHERE id = ? and corpus_id = ?";
		Activity activity = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			stmt.setInt(2, corpus.getId());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				activity = new Activity(rs.getInt("id"), corpus, rs.getString("name"), 
									rs.getString("description"));
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		return activity;
	}
	
	public static Activity FindByName(Connection dbConn, Corpus corpus, String name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT id, name, description FROM activity WHERE name = ? and corpus_id = ?";
		Activity activity = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			stmt.setInt(2, corpus.getId());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				activity = new Activity(rs.getInt("id"), corpus, rs.getString("name"), 
									rs.getString("description")); 
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		return activity;
	}
	
	public void deletePersistence(Connection dbConn) {
		DeletePersistence(dbConn, corpus, id);
	}
	
	public static void DeletePersistence(Connection dbConn, Corpus corpus, int id) {
		final String DELETE_STMT = "DELETE FROM activity WHERE id=? and corpus_id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, id);
			stmt.setInt(2, corpus.getId());
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
	 * @return the parent
	 */
	public Activity getParent() {
		return parent;
	}

	/**
	 * @return ID of any parent, or 0 if no parent
	 */
	@XmlElement
	public int getParentId() {
		return (parent!=null)?parent.getId():0;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Activity parent) {
		this.parent = parent;
	}

	/**
	 * @param child the child to add
	 */
	public void addChild(Activity child) {
		children.add(child);
	}

	/**
	 * @return the number of children activities
	 */
	public int getNChildren() {
		return children.size();
	}

	/**
	 * @return the ith child
	 */
	public Activity getChild(int iChild) {
		return children.get(iChild);
	}

	/**
	 * @return Name.
	 */
	public String toString() {
		return "{id:"+id
		+", name:"+((name==null)?"(null)":name)
		+", corpus:"+((corpus==null)?"(null)":corpus.getId())
		+"}";
	}

}
