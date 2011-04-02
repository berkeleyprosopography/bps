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
	
	public static final int NAME_TYPE_PERSON = 0;
	public static final String NAME_TYPE_PERSON_S = "person";
	public static final int NAME_TYPE_CLAN = 1;
	public static final String NAME_TYPE_CLAN_S = "clan";

	public static final int GENDER_UNKNOWN = 0;
	public static final String GENDER_UNKNOWN_S = "unknown";
	public static final int GENDER_MALE = 1;
	public static final String GENDER_MALE_S = "male";
	public static final int GENDER_FEMALE = 2;
	public static final String GENDER_FEMALE_S = "female";

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
	private int		nametype;
	/**
	 * The gender of this name
	 */
	private int		gender;
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
	
	private int 		docCount;
	private int 		totalCount;
	
	private HashMap<Integer, Integer> countsByDocId;
	
	/**
	 * Create a new empty name.
	 */
	protected Name() {
		this(Name.nextID--, 0, null, NAME_TYPE_PERSON, GENDER_UNKNOWN, null, null, 0, 0);
	}

	/**
	 * Create a new Name.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	protected Name( String name, Corpus corpus) {
		this(Name.nextID--, corpus.getId(), name, NAME_TYPE_PERSON, GENDER_UNKNOWN, 
				null, null, 0, 0);
	}

	protected Name(int id, int corpusId, String name, String nametype, 
			String gender, String notes, Name normal,
			int docCount, int totalCount) {
		this(id, corpusId, name, NameTypeStringToValue(nametype), 
				GenderStringToValue(gender), notes, normal, docCount, totalCount);
	}
	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the Name to be created. Must be unique.
	 * @param name The name as represented in a document
	 * @param notes Researcher notes about this name.
	 * @param normal Reference to the normalized form (null if this is the normal form)
	 */
	public Name(int id, int corpusId, String name, int nametype, 
			int gender, String notes, Name normal,
			int docCount, int totalCount) {
		super();
		this.id = id;
		this.corpusId = corpusId;
		this.name = name;
		this.nametype = nametype;
		this.gender = gender;
		this.notes = notes;
		this.normal = normal;
		this.docCount = docCount;
		this.totalCount = totalCount;
		countsByDocId = new HashMap<Integer, Integer>();
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
	public int getNameType() {
		return nametype;
	}
	
	public static int NameTypeStringToValue(String type) {
		if(NAME_TYPE_PERSON_S.equals(type))
			return NAME_TYPE_PERSON; 
		if(NAME_TYPE_CLAN_S.equals(type))
			return NAME_TYPE_CLAN;
		throw new IllegalArgumentException("Unknown NameType: "+type);
	}
	
	public static String NameTypeToString(int type) {
		return (type==NAME_TYPE_PERSON)?NAME_TYPE_PERSON_S:NAME_TYPE_CLAN_S;
	}
	
	/**
	 * @return the nametype string
	 */
	@XmlElement(name="nametype")
	public String getNameTypeString() {
		return NameTypeToString(nametype);
	}

	/**
	 * @param nametype the nametype to set
	 */
	public void setNameType(int nametype) {
		this.nametype = nametype;
	}

	/**
	 * @return the gender
	 */
	public int getGender() {
		return gender;
	}

	public static int GenderStringToValue(String gender) {
		if(GENDER_UNKNOWN_S.equals(gender))
			return GENDER_UNKNOWN; 
		if(GENDER_MALE_S.equals(gender))
			return GENDER_MALE;
		if(GENDER_FEMALE_S.equals(gender))
			return GENDER_FEMALE;
		throw new IllegalArgumentException("Unknown Gender: "+gender);
	}
	
	public static String GenderToString(int gender) {
		return (gender==GENDER_UNKNOWN)?GENDER_UNKNOWN_S:
				((gender==GENDER_MALE)?GENDER_MALE_S:GENDER_FEMALE_S);
	}
	
	/**
	 * @return the string
	 */
	@XmlElement(name="gender")
	public String getGenderString() {
		return GenderToString(gender);
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(int gender) {
		this.gender = gender;
	}

	/**
	 * @param normal the normal to set
	 */
	public void setNormal(Name normal) {
		this.normal = normal;
	}
	
	public void addCitation(int docId) {
		Integer docCountInMap = countsByDocId.get(docId);
		if(docCountInMap==null)
			docCountInMap = 1;
		else
			docCountInMap++;
		countsByDocId.put(docId, docCountInMap);
		this.docCount = 0;
		this.totalCount = 0;
	}
	
	@XmlElement(name="usedInDocCount")
	public int getDocCount() {
		if(docCount==0) {
			docCount = countsByDocId.keySet().size();
		}
		return docCount;
	}

	@XmlElement(name="usedTotalCount")
	public int getTotalCount() {
		if(totalCount==0) {
			for(Integer perDocCount:countsByDocId.values())
				totalCount += perDocCount;
		}
		return totalCount;
	}

	/**
	 * @return Name.
	 */
	public String toString() {
		return "{"+((name==null)?"(null)":name)+"}";
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
			//"SELECT `id`, `name`,`nametype`,`gender`,`notes`,`normal`"
			//+ "FROM `name` WHERE `corpus_id`=? ORDER BY normal";
			"SELECT n.id, n.name, n.nametype, n.gender, n.notes, n.normal, n.name," 
			+" count(*) totalCount, T2.nDocs docCount FROM name n, name_role_activity_doc nr,"
			+" (SELECT T1.name_id, count(*) nDocs FROM"
			+" (SELECT DISTINCT name_id, document_id FROM name_role_activity_doc) AS T1"
			+" GROUP BY T1.name_id) AS T2"
			+" WHERE n.id=nr.name_id AND n.corpus_id=? AND n.id=T2.name_id GROUP BY n.id" 
			+" ORDER BY n.normal";
		
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
				Name newName = new Name(rs.getInt("id"), corpus_id, 
						rs.getString("name"), rs.getString("nametype"), 
						rs.getString("gender"), rs.getString("notes"), null, 
						rs.getInt("docCount"), rs.getInt("totalCount"));
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
	
	public static List<Name> getFilteredNames(Corpus corpus, String typeFilter, 
			ActivityRole roleFilter, String genderFilter, Connection dbConn) {
		ArrayList<Name> list = new ArrayList<Name>();
		final String SELECT_BY_ROLE = 
			"SELECT distinct n.id FROM name n, name_role_activity_doc nr"
			+" WHERE n.id=nr.name_id AND n.corpus_id=? ";
		final String TYPE_SUFFIX = " AND n.nametype=?";
		final String GENDER_SUFFIX = " AND n.gender=?";
		final String ACT_ROLE_SUFFIX = " AND nr.act_role_id=?";
		StringBuilder sb = new StringBuilder(140);
		sb.append(SELECT_BY_ROLE);
		if(typeFilter!=null) {
			sb.append(TYPE_SUFFIX);
		}
		if(roleFilter!=null) {
			sb.append(ACT_ROLE_SUFFIX);
		}
		if(genderFilter!=null) {
			sb.append(GENDER_SUFFIX);
		}
		try {
			PreparedStatement stmt = dbConn.prepareStatement(sb.toString());
			if(roleFilter!=null) {
				sb.append(ACT_ROLE_SUFFIX);
			}
			stmt.setInt(1, corpus.getId());
			int iNext = 2;
			if(typeFilter!=null) {
				stmt.setString(iNext++, typeFilter);
			}
			if(roleFilter!=null) {
				stmt.setInt(iNext++, roleFilter.getId());
			}
			if(genderFilter!=null) {
				stmt.setString(iNext, genderFilter);
			}
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Name newName = corpus.findName(rs.getInt("id"));
				if(newName==null)
					throw new RuntimeException(
							"Name.getFilteredNames got name: "+rs.getInt("id")
							+" from DB, that corpus: "+corpus.getName()+" cannot find!");
				list.add(newName);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllInCorpus: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
			
		return list;
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
			String name, int nametype, int gender, String notes, Name normal) {
		final String myName = ".CreateAndPersist: ";
		int newId = persistNew(dbConn, corpusId, name, nametype, gender, notes, normal);
		Name newName = new Name(newId, corpusId, name, nametype, gender, notes, normal, 0, 0); 
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
			String name, int nametype, int gender, String notes, Name normal) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO `name`(`name`,`nametype`,`gender`,`notes`,`normal`,`corpus_id`,creation_time)"
			+" VALUES(?,?,?,?,?,?,now())";
			
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, NameTypeToString(nametype));
			stmt.setString(3, GenderToString(gender));
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
			id = persistNew(dbConn, corpusId, name, 
					nametype, gender, 
					notes, normal);
		} else {
			final String UPDATE_STMT = 
				"UPDATE `name`"
				+ " SET `name`=?,`nametype`=?,`gender`=?,`notes`=?,`normal`=?,`corpus_id`=?"
				+ " WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setString(1, name);
				stmt.setString(2, NameTypeToString(nametype));
				stmt.setString(3, GenderToString(gender));
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
			//"SELECT `name`,`nametype`,`gender`,`notes`,`normal`,`corpus_id` FROM `name`"
			//+" WHERE `id`=?";
			"SELECT n.name, n.nametype, n.gender, n.notes, n.normal, n.corpus_id,"
			+" count(*) totalCount, T2.nDocs docCount FROM name n, name_role_activity_doc nr,"
			+" (SELECT T1.name_id, count(*) nDocs FROM"
			+" (SELECT DISTINCT name_id, document_id FROM name_role_activity_doc) AS T1"
			+" WHERE T1.name_id =? GROUP BY T1.name_id) AS T2"
			+" WHERE n.id=nr.name_id AND n.id=T2.name_id AND n.id=?" 
			+" GROUP BY n.id ORDER BY n.normal";

		Name toFind = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_STMT);
			stmt.setInt(1, id);
			stmt.setInt(2, id);
			ResultSet rs = stmt.executeQuery();
			int normalId = 0;
			if(rs.next()){
				toFind = new Name(id, rs.getInt("corpus_id"), rs.getString("name"), 
									rs.getString("nametype"), rs.getString("gender"),
									rs.getString("notes"), null,
									rs.getInt("docCount"), rs.getInt("totalCount"));
				normalId = rs.getInt("normal");
			}
			rs.close();
			if(normalId!=0) {
				stmt.setInt(1, normalId);
				stmt.setInt(2, normalId);
				rs = stmt.executeQuery();
				if(rs.next()){
					// Normal forms do not chain, so we need not recurse.
					Name normalForm = new Name(normalId, rs.getInt("corpus_id"), rs.getString("name"), 
										rs.getString("nametype"), rs.getString("gender"),
										rs.getString("notes"), null,
										rs.getInt("docCount"), rs.getInt("totalCount"));
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
			//"SELECT `id`,`nametype`,`gender`,`notes`,`normal` FROM `name`"
			//+" WHERE `name`=? AND `corpus_id`=?";
			"SELECT n.id, n.nametype, n.gender, n.notes, n.normal, n.corpus_id,"
			+" count(*) totalCount, T2.nDocs docCount FROM name n, name_role_activity_doc nr,"
			+" (SELECT T1.name_id, count(*) nDocs FROM"
			+" (SELECT DISTINCT name_id, document_id FROM name_role_activity_doc) AS T1"
			+" GROUP BY T1.name_id) AS T2"
			+" WHERE n.id=nr.name_id AND n.id=T2.name_id AND n.corpus_id=?" 
			+" AND n.name=? GROUP BY n.id ORDER BY n.normal";

		Name toFind = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_STMT);
			stmt.setInt(1, corpusId);
			stmt.setString(2, name);
			ResultSet rs = stmt.executeQuery();
			int normalId = 0;
			if(rs.next()){
				toFind = new Name(rs.getInt("id"), corpusId, name, 
									rs.getString("nametype"), rs.getString("gender"),
									rs.getString("notes"), null,
									rs.getInt("docCount"), rs.getInt("totalCount"));
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
