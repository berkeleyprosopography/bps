package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.time.TimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="document")
public class Document {
	private final static String myClass = "Document";
	private static int nextId = CachedEntity.UNSET_ID_VALUE;	// temp IDs before we serialize

	@XmlElement
	private int			id;				// Unique numeric id
	private Corpus		corpus;			// Each doc exists in a corpus
	@XmlElement
	private String		alt_id;			// Secondary identifier string
	@XmlElement
	private String		sourceURL;		// TEI source for this document - may be relative
	@XmlElement
	private String		xml_id;			// Element within source (for compound files).
	@XmlElement
	private String		notes;			// Any notes on document
	@XmlElement(name="dateAsEntered")
	private String		date_str;		// Date string from document
	@XmlElement(name="dateValue")
	private long		date_norm;		// Normalized date

	private ArrayList<NameRoleActivity> nameRoleActivities;
	
	public static class AltIdComparator implements	Comparator<Document> {
		public int compare(Document doc1, Document doc2) {
			String altId1 = doc1.getAlt_id();
			String altId2 = doc2.getAlt_id();
			if(altId1==null) {
				return(altId2==null)?0:-1;
			} else if(altId2==null) {
				return 1;
			} else {
				return altId1.compareTo(altId2);
			}
		}
	}
	
	public static class DateComparator implements	Comparator<Document> {
		public int compare(Document doc1, Document doc2) {
			long diff = doc1.getDate_norm()-doc2.getDate_norm();
			return diff==0?0:(diff<0?-1:1);
		}
	}
	
	/**
	 * Ctor with all params - not generally used.
	 * @see Document( String name, String description )
	 * @param id ID of the corpus to be created. Must be unique.
	 * @param alt_id Secondary identifier string
	 * @param source URL TEI source for this document - may be relative
	 * @param xml_id Element within source (for compound files).
	 * @param notes Any notes on document
	 * @param date_str Date string from document
	 * @param date_norm Normalized date
	 */
	public Document(int id, Corpus corpus, String alt_id, String sourceURL, String xml_id,
			String notes, String date_str, long date_norm) {
		this.id = id;
		this.corpus = corpus;
		this.alt_id = alt_id;
		this.sourceURL = sourceURL;
		this.xml_id = xml_id;
		this.notes = notes;
		this.date_str = date_str;
		this.date_norm = date_norm;
		this.nameRoleActivities = new ArrayList<NameRoleActivity>();
	}
	
	public Document cloneInCorpus(Connection dbConn, Corpus newCorpus) {
		Document newDoc = new Document(
				Document.nextId--,
				newCorpus,
				alt_id, sourceURL, xml_id, notes, date_str, date_norm);
		// Get doc id before we create the NRADs
		newDoc.persist(dbConn, CachedEntity.SHALLOW_PERSIST);
		// Clone the NRAD list
		// Maintain a map of old IDs to new NRAD instances, for cloning the NFLs
		HashMap<Integer, NameRoleActivity> context = new HashMap<Integer, NameRoleActivity>();
		Collections.sort(nameRoleActivities);
		for(NameRoleActivity nrad:nameRoleActivities) {
			NameRoleActivity clone = nrad.cloneInDocument(dbConn, newDoc);
			newDoc.nameRoleActivities.add(clone);
			context.put(nrad.getId(), clone);
		}
		for(NameRoleActivity nrad:nameRoleActivities) {
			nrad.cloneNFLs(context.get(nrad.getId()), context);
		}
		// Now persist the NRADs and the NFLs
		newDoc.persist(dbConn, CachedEntity.DEEP_PERSIST);
		return newDoc;
	}

	/**
	 * Create a new Document, and synthesize an ID.
	 * @param alt_id Secondary identifier string
	 * @param source URL TEI source for this document - may be relative
	 * @param xml_id Element within source (for compound files).
	 * @param notes Any notes on document
	 * @param date_str Date string from document
	 * @param date_norm Normalized date
	 */
	public Document(Corpus corpus, String alt_id, String sourceURL, String xml_id,
			String notes, String date_str, long date_norm) {
		this(Document.nextId--, corpus, alt_id, sourceURL, xml_id,
				notes, date_str, date_norm);
	}

	/**
	 * Create a new Document from an alt_id
	 * @param alt_id Secondary identifier string
	 */
	public Document(Corpus corpus, String alt_id, String date, long date_norm) {
		this(Document.nextId--, corpus, alt_id, null, null, null, date, date_norm);
	}

	/**
	 * Create a new null Document.
	 */
	public Document(Corpus corpus) {
		this(Document.nextId--, corpus, null, null, null, null, null, 0);
	}

	/**
	 * Create a new null Document.
	 */
	private Document() {
		this(Document.nextId--, null, null, null, null, null, null, 0);
	}
	
	public void CreateAndPersist(Connection dbConn) {
		//final String myName = ".CreateAndPersist: ";
		id = persistNew(dbConn, corpus.getId(), alt_id, sourceURL, xml_id, 
				notes, date_str, date_norm);
	}
		
	public void persist(Connection dbConn, boolean shallow) {
		final String myName = ".persist: ";
		if(id <= CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, corpus.getId(), alt_id, sourceURL, xml_id, 
					notes, date_str, date_norm);
		} else {
			//System.err.println("Document: "+id+"("+alt_id+") updating.");
			// Note that we do not update the corpus_id - moving them is not allowed 
			final String UPDATE_STMT = 
				"UPDATE document SET alt_id=?, sourceURL=?, xml_id=?, notes=?, "
				+"date_str=?, date_norm=? WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setString(1, alt_id);
				stmt.setString(2, sourceURL);
				stmt.setString(3, xml_id);
				stmt.setString(4, notes);
				stmt.setString(5, date_str);
				stmt.setLong(6, date_norm);
				stmt.setInt(7, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				System.err.println(tmp);
				throw new RuntimeException( tmp );
			}
		}
		if(shallow==CachedEntity.DEEP_PERSIST)
			persistAttachedEntities(dbConn);
	}
		
	private static int persistNew(Connection dbConn, 
			int corpus_id, String alt_id, String sourceURL, String xml_id, 
			String notes, String date_str, long date_norm ) {
		System.err.println("Document: ("+alt_id+") persisting new.");
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO document(corpus_id, alt_id, sourceURL, xml_id, notes, date_str, date_norm, creation_time)"
			+ " VALUES(?,?,?,?,?,?,?,now())";
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, corpus_id);
			stmt.setString(2, alt_id);
			stmt.setString(3, sourceURL);
			stmt.setString(4, xml_id);
			stmt.setString(5, notes);
			stmt.setString(6, date_str);
			stmt.setLong(7, date_norm);
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

	protected void persistNRADs(Connection dbConn) {
		for(NameRoleActivity nrad:nameRoleActivities) {
			nrad.persist(dbConn);
		}
	}
	
	protected void persistNRADAttachedEntities(Connection dbConn) {
		for(NameRoleActivity nrad:nameRoleActivities) {
			nrad.persistAttachedEntities(dbConn);
		}
	}
	
	protected void initAttachedEntityMaps(Connection dbConn) {
		if(id<=0)
			return;
		// Handle NameRoleActivities List
		List<NameRoleActivity> nrads = NameRoleActivity.ListAllInDocument(dbConn, this);
		HashMap<Integer, NameRoleActivity> context = new HashMap<Integer, NameRoleActivity>();
		for(NameRoleActivity nrad:nrads) {
			// TO DO - maintain separate base, clan, and ALL lists.
			nameRoleActivities.add(nrad);
			context.put(nrad.getId(), nrad);
		}
		for(NameRoleActivity nrad:nrads) {
			nrad.initAttachedEntityMaps(dbConn, context);
		}
	}
	
	public void persistAttachedEntities(Connection dbConn) {
		//System.err.println("Document: "+id+"("+alt_id+") persisting NRADS");
		persistNRADs(dbConn);
		persistNRADAttachedEntities(dbConn);
	}
	
	public static Document FindByID(Connection dbConn, Corpus corpus, int docId) {
		final String SELECT_BY_ID = 
			"SELECT id, alt_id, sourceURL, xml_id, notes, date_str"
			+" FROM document WHERE id = ? and corpus_id = ?";
		Document document = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, docId);
			stmt.setInt(2, corpus.getId());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				document = new Document(rs.getInt("id"), corpus, rs.getString("alt_id"), 
						rs.getString("sourceURL"), rs.getString("xml_id"),
						rs.getString("notes"), rs.getString("date_str"), 0);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".FindByID: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return document;
	}

	public static Document FindByAltID(Connection dbConn, Corpus corpus, String altId) {
		final String SELECT_BY_ALT_ID = 
			"SELECT id, alt_id, sourceURL, xml_id, notes, date_str"
			+" FROM document WHERE alt_id = ? and corpus_id = ?";
		Document document = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ALT_ID);
			stmt.setString(1, altId);
			stmt.setInt(2, corpus.getId());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				document = new Document(rs.getInt("id"), corpus, rs.getString("alt_id"), 
						rs.getString("sourceURL"), rs.getString("xml_id"),
						rs.getString("notes"), rs.getString("date_str"), 0);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".FindByID: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return document;
	}

	public static List<Document> ListAllInCorpus(Connection dbConn, Corpus corpus) {
		final String SELECT_BY_CORPUS_ID = 
			"SELECT id, alt_id, sourceURL, xml_id, notes, date_str, date_norm"
			+" FROM document WHERE corpus_id = ?";
		int corpus_id = 0;
		if(corpus==null || (corpus_id=corpus.getId())<=0) {
			String tmp = myClass+".ListAllInCorpus: Invalid corpus.\n";
			System.err.println(tmp);
			throw new IllegalArgumentException( tmp );
		}
		ArrayList<Document> docList = new ArrayList<Document>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_CORPUS_ID);
			stmt.setInt(1, corpus_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Document document = 
					new Document(rs.getInt("id"), corpus, 
						rs.getString("alt_id"), 
						rs.getString("sourceURL"), 
						rs.getString("xml_id"),
						rs.getString("notes"), 
						rs.getString("date_str"), 
						rs.getLong("date_norm"));
				docList.add(document);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllInCorpus: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return docList;
	}
	
	public static void DeleteAllInCorpus(Connection dbConn, Corpus corpus) {
		final String DELETE_ALL = 
			"DELETE FROM document WHERE corpus_id=?";
		int corpus_id = corpus.getId();
		NameRoleActivity.DeleteAllInCorpus(dbConn, corpus);
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
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	@XmlElement(name="inCorpus")
	/**
	 * @return the id of the corpus
	 */
	public int getCorpusId() {
		return (corpus==null)?0:corpus.getId();
	}

	/**
	 * @param corpus the corpus to set
	 */
	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	/**
	 * @return the alt_id
	 */
	public String getAlt_id() {
		return alt_id;
	}

	/**
	 * @param alt_id the alt_id to set
	 */
	public void setAlt_id(String alt_id) {
		this.alt_id = alt_id;
	}

	/**
	 * @return the sourceURL
	 */
	public String getSourceURL() {
		return sourceURL;
	}

	/**
	 * @param sourceURL the sourceURL to set
	 */
	public void setSourceURL(String sourceURL) {
		this.sourceURL = sourceURL;
	}

	/**
	 * @return the xml_id
	 */
	public String getXml_id() {
		return xml_id;
	}

	/**
	 * @param xml_id the xml_id to set
	 */
	public void setXml_id(String xml_id) {
		this.xml_id = xml_id;
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
	 * @return the date_str
	 */
	public String getDate_str() {
		return date_str;
	}

	/**
	 * @param date_str the date_str to set
	 */
	public void setDate_str(String date_str) {
		this.date_str = date_str;
	}

	/**
	 * @return the date_norm
	 */
	public long getDate_norm() {
		return date_norm;
	}
	
	/**
	 * @return the date_norm
	 */
	@XmlElement(name="dateString")
	public String getDate_normAsStr() {
		return (date_norm==0)?null:
			TimeUtils.millisToSimpleYearString(date_norm);
	}


	/**
	 * @param date_norm the date_norm to set
	 */
	public void setDate_norm(long date_norm) {
		this.date_norm = date_norm;
	}

	public void addNameRoleActivity( NameRoleActivity nra ) {
		nameRoleActivities.add(nra);
	}

	/**
	 * @param name The Name involved in this instance
	 * @param role The ActivityRole involved in this instance
	 * @param activity The Activity involved in this instance
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public void addNameRoleActivity(Name name, ActivityRole role,
			Activity activity, String xmlID) {
		addNameRoleActivity(new NameRoleActivity(name, role, activity, xmlID, this));
	}

	/**
	 */
	/**
	 * @param orderForFamily if true, sorts the base (non-family) roles, and
	 * 							then appends the associated family nrads
	 * 							for each base (non-family) role.
	 * @return the nameRoleActivities
	 */
	public List<NameRoleActivity> getNameRoleActivities(boolean orderForFamily) {
		Collections.sort(nameRoleActivities);
		if(!orderForFamily)
			return nameRoleActivities;
		
		List<NameRoleActivity> familyList = new ArrayList<NameRoleActivity>();
		for(NameRoleActivity nrad:nameRoleActivities) {
			if(!nrad.getRole().isFamilyRole()) {
				familyList.add(nrad);
				NameRoleActivity father = nrad.getFather();
				if(father!=null) {
					familyList.add(father);
					NameRoleActivity grandfather = nrad.getGrandFather();
					if(grandfather!=null) {
						familyList.add(grandfather);
						List<NameRoleActivity> ancestors = nrad.getAncestors();
						if(ancestors!=null) {
							for(NameRoleActivity anc:ancestors)
								familyList.add(anc);
						}
					}
				}
				NameRoleActivity clan = nrad.getClan();
				if(clan!=null) {
					familyList.add(clan);
				}
			}
		}
		return familyList;
	}

	/**
	 * @return String description based upon corpus and alt_id
	 */
	public String toString() {
		return "{"+corpus.getName()+':'+((alt_id==null)?"(null)":alt_id)+"}";
	}

	/**
	 * @return String description based upon corpus and alt_id
	 */
	public boolean equals(Document other) {
		return id==other.id;
	}

}
