package edu.berkeley.bps.services.corpus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	private final static String myClass = "Activity";
	private static int	nextID = 1;

	@XmlElement
	private int			id;
	private Corpus		corpus;			// Each activityRole is tied to a corpus
	@XmlElement
	private String		name;
	@XmlElement
	private String		description;

	/**
	 * Create a new empty ActivityRole.
	 */
	public ActivityRole() {
		this(ActivityRole.nextID++, null, null, null);
	}

	/**
	 * Create a new ActivityRole with just a name.
	 * @param name A shorthand name for use in UI, etc.
	 */
	public ActivityRole( Corpus corpus, String name ) {
		this(ActivityRole.nextID++, corpus, name, null);
	}

	/**
	 * Create a new ActivityRole with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public ActivityRole( Corpus corpus, String name, String description ) {
		this(ActivityRole.nextID++, corpus, name, description);
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
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating activity\n"+se.getLocalizedMessage()).build());
		}
		return actRoleList;
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
			((description!=null)?'"'+description+'"':nullStr);
	}

}
