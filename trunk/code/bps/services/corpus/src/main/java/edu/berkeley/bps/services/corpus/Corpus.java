package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.common.time.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
//import javax.xml.xpath.XPath;
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;


/* TODO Next steps:
 * 1) When we create a Corpus from scratch, we should set up the 
 *    docs, activities, etc. as emtpy hashmaps.
 * 2) When we create a Corpus from the DB, we should set up the 
 *    docs, activities, etc. as null pointers, and then
 *    fill them from the DB if they ask for them. This requires adding 
 *    getters on the doc lists, get by id, search, etc. 
 * 3) Need to add a flag to persist to DB or not, as we go.
 * 4) Need to rewrite findOrCreate[Name, Activity, ActivityRole] to persist.
 * 5) Deal with the next steps in Document, Name, NameFamilyLink, NameRoleActivity.
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="corpus")
public class Corpus extends CachedEntity {
	final static String myClass = "Corpus";
	public final static int NO_WKSP_ID = 0;
	
	@XmlElement 
	String		description;
	@XmlElement 
	int			ownerId;
	
	// This is not exposed directly, but only by way of the queries.
	int			wkspId;
	
	private static String myTablename = "corpus";

	private TimeSpan	defaultDocTimeSpan = null;
	// TODO Link to a User instance from bps.services.user.main
	//private User		owner;
	
	// Not multi-instance safe, but these should
	// only be used until docs are persisted.
	private static int nextId = UNSET_ID_VALUE;	// temp IDs before we serialize
	
	private HashMap<Integer, Document> documentsById = null;
	private HashMap<String, Document> documentsByAltId = null;
	
	int fetchedDocumentCount = 0; 
	/**
	 * The named activities (not instances) seen in this corpus
	 */
	private HashMap<String, Activity> activitiesByName = null;
	private HashMap<Integer, Activity> activitiesById = null;

	/**
	 * The named roles in activities (not instances) for this corpus
	 */
	private HashMap<String, ActivityRole> activityRolesByName = null;
	private HashMap<Integer, ActivityRole> activityRolesById = null;
	
	/**
	 * The names seen in this corpus
	 */
	private HashMap<String, Name> namesByName = null;
	private HashMap<Integer, Name> namesById = null;

	/**
	 * Create a new empty corpus.
	 */
	public Corpus() {
		this(Corpus.nextId--, null, null, -1, NO_WKSP_ID, null);
	}

	/**
	 * Create a new corpus with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus( String name, String description, TimeSpan defaultDocTimeSpan ) {
		this(Corpus.nextId--, name, description, -1, NO_WKSP_ID, defaultDocTimeSpan);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see Corpus( String name, String description )
	 * @param id ID of the corpus to be created. Must be unique.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus(int id, String name, String description, 
			int ownerId, int wkspId, TimeSpan defaultDocTimeSpan) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.ownerId = ownerId;
		this.wkspId = wkspId;
		this.defaultDocTimeSpan = defaultDocTimeSpan;
		documentsById = new HashMap<Integer, Document>();
		documentsByAltId = new HashMap<String, Document>();
		activitiesByName = new HashMap<String, Activity>();
		activitiesById = new HashMap<Integer, Activity>();
		activityRolesByName = new HashMap<String, ActivityRole>();
		activityRolesById = new HashMap<Integer, ActivityRole>();
		namesByName = new HashMap<String, Name>();
		namesById = new HashMap<Integer, Name>();
	}
	
	public Corpus cloneInWorkspace(Connection dbConn, 
			int inWkspId, int wkspOwnerId) {
		final String myName = ".cloneInWorkspace: ";
		if(this.wkspId!=NO_WKSP_ID) {
			String tmp = myClass+myName+
				"Cannot clone a workspace-owned Corpus.\n";
			System.err.println(tmp);
			throw new RuntimeException(tmp);
		}
		Corpus newCorpus = CreateAndPersist(dbConn, 
				name, description, wkspOwnerId,
				inWkspId, defaultDocTimeSpan);
		// Clone Activities, and build maps
		for(Activity act:activitiesById.values()) {
			Activity clone = act.cloneInCorpus(dbConn,newCorpus);
			newCorpus.activitiesById.put(clone.getId(), clone);
			newCorpus.activitiesByName.put(clone.getName(), clone);
		}
		// Clone Roles, and build maps
		for(ActivityRole actRole:activityRolesById.values()) {
			ActivityRole clone = actRole.cloneInCorpus(dbConn,newCorpus);
			newCorpus.activityRolesById.put(clone.getId(), clone);
			newCorpus.activityRolesByName.put(clone.getName(), clone);
		}
		// Clone Names, and build maps
		for(Name nameItem:namesById.values()) {
			Name clone = nameItem.cloneInCorpus(dbConn,newCorpus);
			newCorpus.namesById.put(clone.getId(), clone);
			newCorpus.namesByName.put(clone.getName(), clone);
		}
		// Clone Documents, and build maps
		return newCorpus;
	}
	
	private static void initMaps(ServiceContext sc) {
		HashMap<String, Object> nameMap = new HashMap<String, Object>();
		HashMap<Integer, Object> idMap = new HashMap<Integer, Object>();

		Connection dbConn = sc.getConnection(false);
		List<Corpus> corpora = ListAll(dbConn);
		for(Corpus corpus:corpora) {
			String name = corpus.getName();
			if(name!=null&&!name.isEmpty())
				nameMap.put(name, corpus);
			idMap.put(corpus.getId(), corpus);
			corpus.initAttachedEntityMaps(dbConn);
		}
		setNameMap(sc, myClass, nameMap);
		setIdMap(sc, myClass, idMap);
	}
	
	protected void initAttachedEntityMaps(Connection dbConn) {
		// If this is a proper corpus from the DB, then set up the 
		// attached elements as hashmaps
		if(id<=0)
			return;
		System.err.println("Corpus.initAttachedEntityMaps() called...");
		// Handle Activities Map
		activitiesByName.clear();
		activitiesById.clear();
		List<Activity> activitiesList = Activity.ListAllInCorpus(dbConn, this);
		for(Activity act:activitiesList) {
			activitiesByName.put(act.getName(), act);
			activitiesById.put(act.getId(), act);
		}
		System.err.println("Corpus.initAEMaps() Built activities list. Count: "+activitiesByName.size());
		// Handle Activity Roles Map
		activityRolesByName.clear();
		activityRolesById.clear();
		List<ActivityRole> activityRolesList = ActivityRole.ListAllInCorpus(dbConn, this);
		for(ActivityRole actRole:activityRolesList) {
			activityRolesByName.put(actRole.getName(), actRole);
			activityRolesById.put(actRole.getId(), actRole);
		}
		System.err.println("Corpus.initAEMaps() Built roles list. Count: "+activityRolesByName.size());
		// Handle Names Maps
		namesByName.clear();
		namesById.clear();
		List<Name> namesList = Name.ListAllInCorpus(dbConn, this);
		for(Name name:namesList) {
			namesByName.put(name.getName(), name);
			namesById.put(name.getId(), name);
		}
		System.err.println("Corpus.initAEMaps() Built names list. Count: "+namesByName.size());
		// Handle Documents Map
		documentsById.clear();
		documentsByAltId.clear();
		List<Document> docList = Document.ListAllInCorpus(dbConn, this);
		for(Document doc:docList) {
			documentsById.put(doc.getId(), doc);
			doc.initAttachedEntityMaps(dbConn);
			String altId = doc.getAlt_id();
			if(altId!=null&&!altId.isEmpty()) {
				documentsByAltId.put(altId, doc);
			}
		}
		System.err.println("Corpus.initAEMaps() Built doc list. Count: "+documentsById.size());
	}
	
	private void clearMaps() {
		documentsById.clear();
		documentsByAltId.clear();
		activitiesByName.clear();
		activitiesById.clear();
		activityRolesByName.clear();
		activityRolesById.clear();
		namesByName.clear();
		namesById.clear();
	}
	
	
	public static boolean Exists(Connection dbConn, int id) {
		return CachedEntity.Exists(dbConn, myTablename, id);
	}

	public static boolean NameUsed(Connection dbConn, String name) {
		return CachedEntity.NameUsed(dbConn, myTablename, name);
	}

	public static Corpus FindByID(Connection dbConn, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT c.id, c.name, c.description, c.owner_id, c.wksp_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id"
			+" WHERE c.id = ?"
			+" GROUP BY c.id";
		Corpus corpus = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
							rs.getString("description"), 
							rs.getInt("owner_id"), rs.getInt("wksp_id"), null);
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
				corpus.initAttachedEntityMaps(dbConn);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return corpus;
	}

	public static Corpus FindByName(Connection dbConn, String name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT c.id, c.name, c.description, c.owner_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id"
			+" WHERE c.name = ? AND c.wksp_id=" + NO_WKSP_ID
			+" GROUP BY c.id";
		Corpus corpus = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
							rs.getString("description"), 
							rs.getInt("owner_id"), NO_WKSP_ID, null); 
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
				corpus.initAttachedEntityMaps(dbConn);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new RuntimeException( tmp );
		}
		return corpus;
	}

	public static List<Corpus> ListAll(Connection dbConn) {
		return ListAll(dbConn, 0);
	}
	
	public static List<Corpus> ListAll(Connection dbConn, int wkspId) {
		// TODO Add pagination support
		final String SELECT_ALL = 
			"SELECT c.id, c.name, c.description, c.owner_id, c.wksp_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id"
			+" WHERE c.wksp_id=?"
			+" GROUP BY c.id";
		// Generate the right representation according to its media type.
		ArrayList<Corpus> corpusList = new ArrayList<Corpus>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			stmt.setInt(1, wkspId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Corpus corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
						rs.getString("description"), rs.getInt("owner_id"), 
						wkspId, null);
				// If no docId, count should actually be 0, not 1
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
				corpusList.add(corpus);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAll(): Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating corpus\n"+se.getLocalizedMessage()).build());
		}
		return corpusList;
	}	
	/*
	public void CreateAndPersist(Connection dbConn) {
		final String myName = ".CreateAndPersist: ";
		final String INSERT_STMT = 
			"INSERT INTO corpus(name, description, owner_id, creation_time) VALUES(?,?,?,now())";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, ownerId);
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
			System.err.println(tmp);
	    	throw new WebApplicationException( 
	    			Response.status(
	    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}
	*/

	public static Corpus CreateAndPersist(Connection dbConn, 
			String name, String description, int owner_id, TimeSpan defaultDocTimeSpan) {
		return CreateAndPersist(dbConn, 
				name, description, owner_id, NO_WKSP_ID, defaultDocTimeSpan);
	}
	
	protected static Corpus CreateAndPersist(Connection dbConn, 
			String name, String description, int owner_id, int wksp_id, TimeSpan defaultDocTimeSpan) {
		int id = persistNew(dbConn, name, description, owner_id, defaultDocTimeSpan);
		Corpus corpus = new Corpus(id, name, description, 
									owner_id, wksp_id, defaultDocTimeSpan);
		return corpus;
	}
	
	private static int persistNew(Connection dbConn, 
			String name, String description, int owner_id, TimeSpan defaultDocTimeSpan) {
		final String myName = ".persistNew: ";
		// Note that wksp_id defaults to NULL/0
		final String INSERT_STMT = 
			"INSERT INTO corpus(name, description, owner_id, creation_time) VALUES(?,?,?,now())";
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
			throw new RuntimeException( tmp );
		}
		return newId;
	}

	
	public void persist(Connection dbConn, boolean shallow) {
		final String myName = ".persist: ";
		if(id<=UNSET_ID_VALUE) {
			id = persistNew(dbConn, name, description, ownerId, defaultDocTimeSpan);
		} else {
			final String UPDATE_STMT = 
				"UPDATE corpus SET name=?, description=? WHERE id=?";
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
		if(shallow==CachedEntity.DEEP_PERSIST)
			persistAttachedEntities(dbConn);
	}
	
	protected void persistNames(Connection dbConn) {
		// Since the maps are parallel, we only iterate over one
		for(Name name:namesByName.values()) {
			boolean fResetId = (name.getId()<=CachedEntity.UNSET_ID_VALUE);
			if(fResetId) {
				namesById.remove(name.getId());
			}
			name.persist(dbConn);
			if(fResetId) {
				namesById.put(name.getId(), name);
			}
		}
	}

	protected void persistActivities(Connection dbConn) {
		for(Activity activity:activitiesByName.values()) {
			boolean fResetId = (activity.getId()<=CachedEntity.UNSET_ID_VALUE);
			if(fResetId) {
				activitiesById.remove(activity.getId());
			}
			activity.persist(dbConn);
			if(fResetId) {
				activitiesById.put(activity.getId(), activity);
			}
		}
	}
	
	protected void persistActivityRoles(Connection dbConn) {
		for(ActivityRole activityRole:activityRolesByName.values()) {
			boolean fResetId = (activityRole.getId()<=CachedEntity.UNSET_ID_VALUE);
			if(fResetId) {
				activityRolesById.remove(activityRole.getId());
			}
			activityRole.persist(dbConn);
			if(fResetId) {
				activityRolesById.put(activityRole.getId(), activityRole);
			}
		}
	}
	
	protected void persistDocuments(Connection dbConn) {
		for(Document doc:documentsById.values()) {
			doc.persist(dbConn, CachedEntity.DEEP_PERSIST);
		}
	}
	
	public void persistAttachedEntities(Connection dbConn) {
		persistNames(dbConn);
		persistActivities(dbConn);
		persistActivityRoles(dbConn);
		persistDocuments(dbConn);
	}
	
	public void deletePersistence(Connection dbConn) {
		deleteAttachedEntities(dbConn);
		DeletePersistence(dbConn, id);
	}
	
	private static void DeletePersistence(Connection dbConn, int id) {
		final String DELETE_STMT = "DELETE FROM corpus WHERE id=?";
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

	/*
	public static Corpus CreateFromTEI(org.w3c.dom.Document docNode, boolean deepCreate,
			TimeSpan defaultDocTimeSpan, Connection dbConn)
		throws XPathExpressionException {
		String name = "unknown";
		String description = null;
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    Corpus newCorpus = null;
       // XPath Query to get to the corpus title
	    try {
		    XPathExpression expr = xpath.compile("//teiHeader/fileDesc/titleStmt/title");
		    Element titleEl = (Element) expr.evaluate(docNode, XPathConstants.NODE);
		    if(titleEl!=null)
		    	name = titleEl.getTextContent().replaceAll("[\\s]+", " ");
		    expr = xpath.compile("//teiHeader/fileDesc/sourceDesc/p");
		    Element descEl = (Element) expr.evaluate(docNode, XPathConstants.NODE);
		    if(descEl!=null)
		    	description = descEl.getTextContent().replaceAll("[\\s]+", " ");
		    newCorpus = new Corpus(name, description, defaultDocTimeSpan);
		    if(deepCreate) {
		    	// Find the TEI nodes and create a document for each one
		    	NodeList docNodes = docNode.getElementsByTagName( "TEI" );
				if( docNodes.getLength() < 1 ) {  // Must define at least one.
					System.err.println("Corpus:CreateFromTEI: Corpus file has no TEI elements!");
				} else {
					// For each info element, need to get all the fields.
					int nDocs = docNodes.getLength();
					for( int iDoc = 0; iDoc < nDocs; iDoc++) {
					    Element teiEl = (Element)docNodes.item(iDoc);
					    Document document = Document.CreateAndPersistFromTEI(teiEl, true, newCorpus, dbConn);
					    newCorpus.addDocument(document);
					}
				}
		    }
	    } catch (XPathExpressionException xpe) {
	    	// debug complaint
	    	throw xpe;
	    }
	    return newCorpus;
	}
	*/

	/**
	 * Deletes all current documents, and then rebuilds corpus from TEI
	 * @param docNode The TEI document to load this corpus from
	 * @param deepCreate If TRUE, will create documents as well.
	 * @param dbConn Connection for persistence
	 * @return count of documents created
	 * @throws XPathExpressionException
	 */
	/*
	public int loadFromTEI(org.w3c.dom.Document docNode, boolean deepCreate,
			Connection dbConn)
		throws XPathExpressionException {
		String name = "unknown";
		String description = null;
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    // Clear all current documents
    	deleteDocuments(dbConn);
	    
	    try {
	        // XPath Query to get to the corpus title
		    XPathExpression expr = xpath.compile("//teiHeader/fileDesc/titleStmt/title");
		    Element titleEl = (Element) expr.evaluate(docNode, XPathConstants.NODE);
		    if(titleEl!=null)
		    	name = titleEl.getTextContent().replaceAll("[\\s]+", " ");
		    expr = xpath.compile("//teiHeader/fileDesc/sourceDesc/p");
		    Element descEl = (Element) expr.evaluate(docNode, XPathConstants.NODE);
		    if(descEl!=null)
		    	description = descEl.getTextContent().replaceAll("[\\s]+", " ");
		    if(deepCreate) {
		    	// Find the TEI nodes and create a document for each one
		    	NodeList docNodes = docNode.getElementsByTagName( "TEI" );
				if( docNodes.getLength() < 1 ) {  // Must define at least one.
					System.err.println("Corpus:CreateFromTEI: Corpus file has no TEI elements!");
				} else {
					// For each info element, need to get all the fields.
					int nDocs = docNodes.getLength();
					for( int iDoc = 0; iDoc < nDocs; iDoc++) {
					    Element teiEl = (Element)docNodes.item(iDoc);
					    Document document = Document.CreateAndPersistFromTEI(teiEl, true, this, dbConn);
					    addDocument(document);
					}
				}
		    }
		    persist(dbConn);
	    } catch (XPathExpressionException xpe) {
	    	// debug complaint
	    	throw xpe;
	    }
	    return documents.size();
	}
	*/

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	@XmlElement(name="ndocs")
	public int getNDocuments() {
		int nDocs = 0;
		if(documentsById != null) {
			nDocs = documentsById.size();
		} else {
			nDocs = fetchedDocumentCount;
		}
		return nDocs;
	}
	
	@XmlElement(name="medianDocDate")
	public String getMedianDocumentDate() {
		long medianDate = 0;
		if(documentsById != null) {
			ArrayList<Long> validDates = new ArrayList<Long>(documentsById.size());
			for(Document doc:documentsById.values()) {
				long docDate = doc.getDate_norm();
				if(docDate != 0) {
					validDates.add(docDate);
				}
			}
			if(!validDates.isEmpty()) {
				Collections.sort(validDates);
				if (validDates.size() % 2 == 1) {
					medianDate = validDates.get((validDates.size()+1)/2-1);
				} else {
					long lower = validDates.get(validDates.size()/2-1);
					long upper = validDates.get(validDates.size()/2);
					medianDate = (lower + upper) / 2L;
			    }	
			}
		}
		return (medianDate==0)?null:
			TimeUtils.millisToSimpleYearString(medianDate);
	}
	
	public List<Document> getDocuments() {
		ArrayList<Document> list = new ArrayList<Document>(documentsById.values());
		return list;
	}
	
	public Document getDocument(int docId) {
		Document doc = documentsById.get(docId);
		return doc;
	}
	
	public Document getDocumentByAltId(String altId) {
		Document doc = documentsByAltId.get(altId);
		return doc;
	}
	
	public List<Activity> getActivities() {
		ArrayList<Activity> list = new ArrayList<Activity>(activitiesByName.values());
		return list;
	}
	
	public List<ActivityRole> getActivityRoles() {
		ArrayList<ActivityRole> list = 
			new ArrayList<ActivityRole>(activityRolesByName.values());
		return list;
	}
	
	public List<Name> getNames() {
		ArrayList<Name> list = new ArrayList<Name>(namesByName.values());
		return list;
	}
	
	/*
	private void fetchDocumentCount(Connection dbConn) {
		final String SELECT_N_DOCS = 
			"SELECT count(*) nDocs FROM document WHERE corpus_id = ?";
		int nDocs = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_N_DOCS);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				fetchedDocumentCount = rs.getInt("nDocs");
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			// Just absorb it
			String tmp = myClass+".getNDocuments: Problem querying DB.\n"+ se.getMessage();
			System.err.println(tmp);
		}
	}
	*/
	
	public void loadDocuments(Connection dbConn) {
		throw new RuntimeException("Not Yet Implemented");
	}

	public void addDocument( Document newDoc ) {
		documentsById.put(newDoc.getId(), newDoc);
		String altId = newDoc.getAlt_id();
		if(altId!=null&&!altId.isEmpty()) {
			documentsByAltId.put(altId, newDoc);
		}

	}
	
	public void deleteAttachedEntities(Connection dbConn) {
		// Clear out the existing documents, Names, etc. 
        deleteDocuments(dbConn);
        // This is probably not necessary, but is cheap
        deleteActivities(dbConn);
        // This is probably not necessary, but is cheap
        deleteActivityRoles(dbConn);
        deleteNames(dbConn);

	}
	
	public void deleteDocuments(Connection dbConn) {
		Document.DeleteAllInCorpus(dbConn, this);
		if(documentsById!=null)
			documentsById.clear();
		if(documentsByAltId!=null)
			documentsByAltId.clear();
	}

	public void deleteActivities(Connection dbConn) {
		Activity.DeleteAllInCorpus(dbConn, this);
		if(activitiesByName!=null)
			activitiesByName.clear();
		if(activitiesById!=null)
			activitiesById.clear();
	}

	public void deleteNames(Connection dbConn) {
		Name.DeleteAllInCorpus(dbConn, this);
		if(namesByName!=null)
			namesByName.clear();
		if(namesById!=null)
			namesById.clear();
	}

	public void deleteActivityRoles(Connection dbConn) {
		ActivityRole.DeleteAllInCorpus(dbConn, this);
		if(activityRolesByName!=null)
			activityRolesByName.clear();
		if(activityRolesById!=null)
			activityRolesById.clear();
	}

	public Name findOrCreateName(String name, Connection dbConn) {
		Name instance = namesByName.get(name);
		if(instance == null) {
			instance = Name.CreateAndPersist(dbConn, id, name, 
					Name.NAME_TYPE_PERSON, Name.GENDER_UNKNOWN, null, null);
			addName(instance);
		}
		return instance;
	}
	
	public void addName(Name toAdd) {
		if(namesByName.get(toAdd.getName())!=null)
			throw new RuntimeException("Corpus.addName: duplicate (name).");
		if(namesById.get(toAdd.getId())!=null)
			throw new RuntimeException("Corpus.addName: duplicate (id).");
		namesByName.put(toAdd.getName(), toAdd);
		namesById.put(toAdd.getId(), toAdd);
	}

	public Name findName(String name) {
		Name instance = namesByName.get(name);
		return instance;
	}

	public Name findName(int nid) {
		Name instance = namesById.get(nid);
		return instance;
	}

	public Activity findOrCreateActivity(String name, Connection dbConn) {
		Activity instance = activitiesByName.get(name);
		if(instance == null) {
			instance = Activity.CreateAndPersist(dbConn, this, name, null);
			addActivity(instance);
		}
		return instance;
	}

	public void addActivity(Activity toAdd) {
		if(activitiesByName.get(toAdd.getName())!=null)
			throw new RuntimeException("Corpus.addActivity: duplicate (name).");
		if(activitiesById.get(toAdd.getId())!=null)
			throw new RuntimeException("Corpus.addActivity: duplicate (id).");
		activitiesByName.put(toAdd.getName(), toAdd);
		activitiesById.put(toAdd.getId(), toAdd);
	}

	public Activity findActivity(String name) {
		Activity instance = activitiesByName.get(name);
		return instance;
	}

	public Activity findActivity(int aid) {
		Activity instance = activitiesById.get(aid);
		return instance;
	}

	public ActivityRole findOrCreateActivityRole(String name, Connection dbConn) {
		ActivityRole instance = activityRolesByName.get(name);
		if(instance == null) {
			instance = ActivityRole.CreateAndPersist(dbConn, this, name, null);
			instance.persist(dbConn);
			addActivityRole(instance);
		}
		return instance;
	}

	public void addActivityRole(ActivityRole toAdd) {
		if(activityRolesByName.get(toAdd.getName())!=null)
			throw new RuntimeException("addActivityRole: duplicate (name).");
		if(activityRolesById.get(toAdd.getId())!=null)
			throw new RuntimeException("addActivityRole: duplicate (id).");
		activityRolesByName.put(toAdd.getName(), toAdd);
		activityRolesById.put(toAdd.getId(), toAdd);
	}

	public ActivityRole findActivityRole(String name) {
		ActivityRole instance = activityRolesByName.get(name);
		return instance;
	}

	public ActivityRole findActivityRole(int arid) {
		ActivityRole instance = activityRolesById.get(arid);
		return instance;
	}

	// TODO move this to the SQL Utils class - it should not be here. Provide read accessors
	// to the various maps as needed.
	public void generateDependentSQL(
			String documentsFilename,
			String activitiesFilename,
			String namesFilename,
			String nameFamilyLinksFilename,
			String activityRolesFilename,
			String nameRoleActivitiesFilename ) {
		//initMaps();
    	System.out.print("Generating Documents (and NameRoleActivityDocs) SQL...");
		SQLUtils.generateDocumentsSQL(documentsFilename,
				nameRoleActivitiesFilename, nameFamilyLinksFilename, documentsById);
    	System.out.println("Done.");
    	System.out.print("Generating Activities SQL...");
		SQLUtils.generateActivitiesSQL(activitiesFilename, activitiesByName);
    	System.out.println("Done.");
    	System.out.print("Generating ActivityRoles SQL...");
		SQLUtils.generateActivityRolesSQL(activityRolesFilename, activityRolesByName);
    	System.out.println("Done.");
    	System.out.println("Done.");
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
	
	public Element toXMLPayload(org.w3c.dom.Document doc, boolean includeDetails) {
		return toXMLPayload(doc, id, name, description, null);
	}
	
	public static Element toXMLPayload(org.w3c.dom.Document doc, 
			int id, String name, String description, String owner ) {
        Element corpusEl = doc.createElement("corpus");

        if(id > 0) {
        	Element eltId = doc.createElement("id");
        	eltId.appendChild(doc.createTextNode(Integer.toString(id)));
        	corpusEl.appendChild(eltId);
        }
        if(name != null) {
        	Element eltName = doc.createElement("name");
        	eltName.appendChild(doc.createTextNode(name));
        	corpusEl.appendChild(eltName);
        }
        if(description != null) {
        	Element eltDescription = doc.createElement("description");
        	eltDescription.appendChild(doc.createTextNode(description));
        	corpusEl.appendChild(eltDescription);
        }
        if(owner != null) {
        	Element eltOwner = doc.createElement("owner");
        	eltOwner.appendChild(doc.createTextNode(owner));
        	corpusEl.appendChild(eltOwner);
        }
        return corpusEl;
	}

	/**
	 * @return the defaultDocTimeSpan
	 */
	@XmlTransient
	public TimeSpan getDefaultDocTimeSpan() {
		return defaultDocTimeSpan;
	}

	/**
	 * @param defaultDocTimeSpan the defaultDocTimeSpan to set
	 */
	public void setDefaultDocTimeSpan(TimeSpan defaultDocTimeSpan) {
		this.defaultDocTimeSpan = defaultDocTimeSpan;
	}
}
