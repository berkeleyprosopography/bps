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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="activity")
public class Activity {
	private final static String myClass = "Activity";
	private final static String DELETE_STMT = "DELETE FROM activity WHERE id=?";
	private static int	nextID = 1;

	@XmlElement
	private int			id;
	@XmlElement
	private String		name;
	@XmlElement
	private String		description;
	// Not fully supported yet
	private Activity	parent;
	private ArrayList<Activity>	children;

	public Activity() {
		this(0,null,null,null);
	}

	/**
	 * @see Activity( String name, String description, Activity parent )
	 * @param id
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 * @param parent Broader activity that this specializes.
	 */
	private Activity(int id, String name, String description, Activity parent) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.parent = parent;
		if(parent!=null)
			parent.addChild(this);
	}

	/**
	 * @see Activity( String name, String description, Activity parent )
	 * @param id
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Activity(int id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public void CreateAndPersist(Connection dbConn) {
		final String myName = ".CreateAndPersist: ";
		final String INSERT_STMT = 
			"INSERT INTO activity(name, description, parent_id, creation_time) VALUES(?,?,?,now())";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, description);
			if(parent==null) {
				stmt.setNull(3, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(3, parent.getId());
			}
			int nRows = stmt.executeUpdate();
			if(nRows==1){
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next()){
					id = rs.getInt(1); 
				}
				rs.close();
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}
		
	public static Activity CreateAndPersist(Connection dbConn, 
			String name, String description) {
		return CreateAndPersist(dbConn, name, description, null);
	}
	
	private static Activity CreateAndPersist(Connection dbConn, 
			String name, String description, Activity parent) {
		final String myName = ".CreateAndPersist: ";
		final String INSERT_STMT = 
			"INSERT INTO activity(name, description, parent_id, creation_time) VALUES(?,?,?,now())";
		Activity activity = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, description);
			if(parent==null) {
				stmt.setNull(3, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(3, parent.getId());
			}
			int nRows = stmt.executeUpdate();
			if(nRows==1){
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next()){
					activity = new Activity(rs.getInt(1), name, description, parent); 
				}
				rs.close();
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return activity;
	}
	
	public static List<Activity> ListAll(Connection dbConn) {
		// TODO Add pagination support
		// TODO rebuild parent structures from DB
		final String SELECT_ALL = 
			"SELECT id, name, description FROM activity";
		ArrayList<Activity> activityList = new ArrayList<Activity>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Activity activity = new Activity(rs.getInt("id"), rs.getString("name"), 
						rs.getString("description"));
				activityList.add(activity);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAll(): Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating activity\n"+se.getLocalizedMessage()).build());
		}
		return activityList;
	}
	
	public static boolean Exists(Connection dbConn, int id) {
		boolean exists = false;
		final String SELECT_BY_ID = "SELECT name FROM activity WHERE id = ?";
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
			System.out.println(tmp);
		}
		return exists;
	}

	public static Activity FindByID(Connection dbConn, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT id, name, description FROM activity WHERE id = ?";
		Activity activity = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				activity = new Activity(rs.getInt("id"), rs.getString("name"), 
									rs.getString("description"));
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return activity;
	}
	
	public static Activity FindByName(Connection dbConn, String name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT id, name, description FROM activity WHERE name = ?";
		Activity activity = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				activity = new Activity(rs.getInt("id"), rs.getString("name"), 
									rs.getString("description")); 
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return activity;
	}
	
	public void persist(Connection dbConn) {
		final String myName = ".persist: ";
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
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
	}
	
	public void deletePersistence(Connection dbConn) {
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
	
	public static void DeletePersistence(Connection dbConn, int id) {
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
		return "{"+((name==null)?"(null)":name)+"}";
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @param nullStr The null indicator to use for missing entries
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep, String nullStr ) {
		return id+sep+
			((name!=null)?'"'+name+'"':nullStr)+sep+
			((description!=null)?'"'+description+'"':nullStr)+sep+
			((parent==null)?"\\N":parent.id);
	}

}
