package edu.berkeley.bps.services.corpus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author pschmitz
 *
 */

// TODO
// Finish alignment to schema, then write persist, and a CreateAndPersist factory method,
// which also handles setting the id correctly.

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="name")
public class Name {
	private final static String myClass = "Name";
	private static int	nextID = CachedEntity.UNSET_ID_VALUE;
	
	public static final String NAME_TYPE_PERSON = "person";
	public static final String NAME_TYPE_CLAN = "clan";

	public static final String GENDER_MALE = "male";
	public static final String GENDER_FEMALE = "female";
	public static final String GENDER_UNKNOWN = "unknown";

	/**
	 * The internal DB id, or could be a UUID
	 */
	@XmlElement
	private int			id;
	/**
	 * The associated corpus, if non-zero. If 0, indicates no corpus link
	 */
	@XmlElement
	private int			corpusId;
	/**
	 * The form of this name
	 */
	@XmlElement
	private String		name;
	/**
	 * The type of this name
	 */
	@XmlElement
	private String		nametype;
	/**
	 * The gender of this name
	 */
	@XmlElement
	private String		gender;
	/**
	 * Any notes about this form
	 */
	@XmlElement
	private String		notes;
	/**
	 * If this is not the normalized form, then normal is a reference to the normalized form.
	 * If normal is null, this is a normalized form.
	 */
	private Name		normal;
	
	/**
	 * Create a new empty name.
	 */
	private Name() {
		this(Name.nextID--, 0, null, NAME_TYPE_PERSON, GENDER_UNKNOWN, null, null);
	}

	/**
	 * Create a new Name.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Name( String name, Corpus corpus) {
		this(Name.nextID--, corpus.getId(), name, NAME_TYPE_PERSON, GENDER_UNKNOWN, null, null);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the Name to be created. Must be unique.
	 * @param name The name as represented in a document
	 * @param notes Researcher notes about this name.
	 * @param normal Reference to the normalized form (null if this is the normal form)
	 */
	public Name(int id, int corpusId, String name, String nametype, String gender, String notes, Name normal) {
		super();
		this.id = id;
		this.corpusId = corpusId;
		this.name = name;
		this.nametype = nametype;
		this.gender = gender;
		this.notes = notes;
		this.normal = normal;
	}
	
	public Name cloneInCorpus(Connection dbConn, Corpus newCorpus) {
		final String myName = ".cloneInCorpus: ";
		if(normal!=null) {
			String tmp = myClass+myName+"Cannot clone Name with normal form (NYI).\n";
			System.err.println(tmp);
			throw new RuntimeException(tmp);
		}
		return CreateAndPersist(dbConn, newCorpus.getId(),
				name, nametype, gender, notes, null);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set. Note that this is not updated on calls to persist(), 
	 * as it is assumed to be set by the DB on create, or passed to create.  
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the corpusId
	 */
	public int getCorpusId() {
		return corpusId;
	}

	/**
	 * @param corpusId the corpusId to set
	 */
	public void setCorpusId(int corpusId) {
		this.corpusId = corpusId;
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
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the normal
	 */
	public Name getNormal() {
		return normal;
	}

	/**
	 * @return the normal
	 */
	@XmlElement(name="normal")
	public int getNormalId() {
		return (normal==null)?0:normal.id;
	}
	/**
	 * @return the nametype
	 */
	public String getNameType() {
		return nametype;
	}

	/**
	 * @param nametype the nametype to set
	 */
	public void setNameType(String nametype) {
		this.nametype = nametype;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @param normal the normal to set
	 */
	public void setNormal(Name normal) {
		this.normal = normal;
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
			((notes!=null)?'"'+notes+'"':nullStr)+sep+
			((normal!=null)?normal.id:nullStr);
	}

	public boolean equals(Name compareTo) {
		if(id==compareTo.id)
			return true;
		// Check this normal form against compareTo and its normal
		if(normal!=null) {
			if(normal.id==compareTo.id) {
				return true;
			} else if((compareTo.normal!=null)&&(compareTo.normal.id==normal.id)) {
				return true;
			}
		// Check this against compareTo normal
		} else if((compareTo.normal!=null)&&(compareTo.normal.id==id)) {
			return true;
		}
		return false;
	}

	public static List<Name> ListAllInCorpus(Connection dbConn, Corpus corpus) {
		// When we select them, we need to order by normal. 
		// Assume null normal values sort first, and that there are no chains.
		final String SELECT_BY_CORPUS_ID = 
			"SELECT `id`, `name`,`nametype`,`gender`,`notes`,`normal`,`corpus_id`"
			+ "FROM `name` WHERE `corpus_id`=? ORDER BY normal";
		int corpus_id = 0;
		if(corpus==null || (corpus_id=corpus.getId())<=0) {
			String tmp = myClass+".ListAllInCorpus: Invalid corpus.\n";
			System.err.println(tmp);
			throw new IllegalArgumentException( tmp );
		}
		ArrayList<Name> nameList = new ArrayList<Name>();
		HashMap<Integer, Name> nameMap = new HashMap<Integer, Name>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_CORPUS_ID);
			stmt.setInt(1, corpus_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Name newName = new Name(rs.getInt("id"), rs.getInt("corpus_id"), 
						rs.getString("name"), rs.getString("nametype"), 
						rs.getString("gender"), rs.getString("notes"), null);
				int normalId = rs.getInt("normal");
				if(normalId != 0) {
					Name normal = nameMap.get(normalId);
					if(normal==null) {
						throw new RuntimeException(myClass+".ListAllInCorpus:"
							+" Internal error: Could not find normal form of name in map!");
					}
					newName.setNormal(normal);
				}
				nameList.add(newName);
				nameMap.put(newName.id, newName);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllInCorpus: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return nameList;
	}
	
	public static void DeleteAllInCorpus(Connection dbConn, Corpus corpus) {
		final String DELETE_ALL = 
			"DELETE FROM `name` WHERE corpus_id=?";
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
							"Problem deleting documents\n"+se.getLocalizedMessage()).build());
		}
	}
	
	/**
	 * Creates a new Name entity, persists to the DB store, and sets the created ID. 
	 * @param dbConn an open JDCB connection
	 * @param corpusId 0 if a generic Name, else set to a linked corpus
	 * @param name The name form
	 * @param nametype One of NAME_TYPE_PERSON or NAME_TYPE_CLAN
	 * @param gender One of GENDER_MALE, GENDER_FEMALE, or GENDER_UNKNOWN
	 * @param notes Any notes on form, etc.
	 * @param normal The normal form of this name, if 'name' is not the normal form. 
	 * @return
	 */
	public static Name CreateAndPersist(Connection dbConn, int corpusId,
			String name, String nametype, String gender, String notes, Name normal) {
		final String myName = ".CreateAndPersist: ";
		int newId = persistNew(dbConn, corpusId, name, nametype, gender, notes, normal);
		Name newName = new Name(newId, corpusId, name, nametype, gender, notes, normal); 
		return newName;
	}
	
	/**
	 * Persists a new Name entity to the DB store, and returns the created ID. 
	 * @param dbConn an open JDCB connection
	 * @param corpusId 0 if a generic Name, else set to a linked corpus
	 * @param name The name form
	 * @param nametype One of NAME_TYPE_PERSON or NAME_TYPE_CLAN
	 * @param gender One of GENDER_MALE, GENDER_FEMALE, or GENDER_UNKNOWN
	 * @param notes Any notes on form, etc.
	 * @param normal The normal form of this name, if 'name' is not the normal form. 
	 * @return
	 */
	public static int persistNew(Connection dbConn, int corpusId,
			String name, String nametype, String gender, String notes, Name normal) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO `name`(`name`,`nametype`,`gender`,`notes`,`normal`,`corpus_id`,creation_time)"
			+" VALUES(?,?,?,?,?,?,now())";
			
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, nametype);
			stmt.setString(3, gender);
			stmt.setString(4, notes);
			if(normal==null) {
				stmt.setNull(5, Types.INTEGER);
			} else {
				stmt.setInt(5, normal.id);
			}
			stmt.setInt(6, corpusId);
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
			throw new RuntimeException( tmp );
		}
		return newId;
	}
	
	public void persist(Connection dbConn) {
		final String myName = ".persist: ";
		if(id<=CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, corpusId, name, nametype, gender, notes, normal);
		} else {
			final String UPDATE_STMT = 
				"UPDATE `name`"
				+ " SET `name`=?,`nametype`=?,`gender`=?,`notes`=?,`normal`=?,`corpus_id`=?"
				+ " WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setString(1, name);
				stmt.setString(2, nametype);
				stmt.setString(3, gender);
				stmt.setString(4, notes);
				if(normal==null) {
					stmt.setNull(5, Types.INTEGER);
				} else {
					stmt.setInt(5, normal.id);
				}
				stmt.setInt(6, corpusId);
				stmt.setInt(7, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				System.err.println(tmp);
				throw new RuntimeException( tmp );
			}
		}
	}
	
	/**
	 * @param dbConn an open JDBC connection
	 * @param id DB id of the Name to find
	 * @return
	 */
	public static Name FindById(Connection dbConn, int id) {
		final String myName = ".FindById: ";
		final String SELECT_STMT = 
			"SELECT `name`,`nametype`,`gender`,`notes`,`normal`,`corpus_id` FROM `name`"
			+" WHERE `id`=?";

		Name toFind = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_STMT);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			int normalId = 0;
			if(rs.next()){
				toFind = new Name(id, rs.getInt("corpus_id"), rs.getString("name"), 
									rs.getString("nametype"), rs.getString("gender"),
									rs.getString("notes"), null);
				normalId = rs.getInt("normal");
			}
			rs.close();
			if(normalId!=0) {
				stmt.setInt(1, normalId);
				rs = stmt.executeQuery();
				if(rs.next()){
					// Normal forms do not chain, so we need not recurse.
					Name normalForm = new Name(normalId, rs.getInt("corpus_id"), rs.getString("name"), 
										rs.getString("nametype"), rs.getString("gender"),
										rs.getString("notes"), null);
					toFind.setNormal(normalForm);
				}
				rs.close();
			}
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return toFind;
	}

	public static boolean Exists(Connection dbConn, Corpus corpus, int id) {
		boolean exists = false;
		final String SELECT_BY_ID = 
			"SELECT `name` FROM name WHERE id = ? and corpus_id = ?";
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

	/**
	 * @param dbConn an open JDBC connection
	 * @param id DB id of the Name to find
	 * @param forCorpusId 0 if matches any generic Name, or >0 to match for a corpus
	 * @return
	 */
	public static Name FindByName(Connection dbConn, String name, int corpusId) {
		final String myName = ".FindByName: ";
		final String SELECT_STMT = 
			"SELECT `id`,`nametype`,`gender`,`notes`,`normal` FROM `name`"
			+" WHERE `name`=? AND `corpus_id`=?";

		Name toFind = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_STMT);
			stmt.setString(1, name);
			stmt.setInt(2, corpusId);
			ResultSet rs = stmt.executeQuery();
			int normalId = 0;
			if(rs.next()){
				toFind = new Name(rs.getInt("id"), corpusId, name, 
									rs.getString("nametype"), rs.getString("gender"),
									rs.getString("notes"), null);
				normalId = rs.getInt("normal");
			}
			rs.close();
			stmt.close();
			if(normalId!=0) {
				if(rs.next()){
					// Normal forms do not chain, so we need not recurse.
					Name normalForm = FindById(dbConn, normalId);
					toFind.setNormal(normalForm);
				}
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return toFind;
	}

	public void deletePersistence(Connection dbConn) {
		DeletePersistence(dbConn, corpusId, id);
	}
	
	public static void DeletePersistence(Connection dbConn, int corpus_id, int id) {
		final String DELETE_STMT = "DELETE FROM name WHERE id=? and corpus_id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, id);
			stmt.setInt(2, corpus_id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".DeletePersistence: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
	}
}
