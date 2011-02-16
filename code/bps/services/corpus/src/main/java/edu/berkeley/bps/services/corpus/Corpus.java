package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.common.time.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


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
	
	private final static String DELETE_STMT = "DELETE FROM corpus WHERE id=?";

	private static int	nextID = 1;

	@XmlElement String		description;
	@XmlElement int			ownerId;
	
	private static String myTablename = "corpus";

	private TimeSpan	defaultDocTimeSpan = null;
	// TODO Link to a User instance from bps.services.user.main
	//private User		owner;

	
	HashMap<Integer, Document> documents = null;
	
	int fetchedDocumentCount = 0; 
	/**
	 * The named activities (not instances) seen in this corpus
	 */
	private HashMap<String, Activity> activities = null;
	/**
	 * The named roles in activities (not instances) for this corpus
	 */
	private HashMap<String, ActivityRole> activityRoles = null;

	/**
	 * Create a new empty corpus.
	 */
	public Corpus() {
		this(0, null, null, -1, null);
	}

	/**
	 * Create a new corpus with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus( String name, String description, TimeSpan defaultDocTimeSpan ) {
		this(Corpus.nextID++, name, description, -1, defaultDocTimeSpan);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see Corpus( String name, String description )
	 * @param id ID of the corpus to be created. Must be unique.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus(int id, String name, String description, 
			int ownerId, TimeSpan defaultDocTimeSpan) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.ownerId = ownerId;
		this.defaultDocTimeSpan = defaultDocTimeSpan;
	}
	
	private static void initMaps(ServiceContext sc) {
		HashMap<String, Object> nameMap = new HashMap<String, Object>();
		HashMap<Integer, Object> idMap = new HashMap<Integer, Object>();

		{
			Connection dbConn = sc.getConnection(false);
			List<Corpus> corpora = ListAll(dbConn);
			for(Corpus corpus:corpora) {
				String name = corpus.getName();
				if(name!=null&&!name.isEmpty())
					nameMap.put(name, corpus);
				idMap.put(corpus.getId(), corpus);
				corpus.documents = new HashMap<Integer, Document>();
				List<Document> docList = Document.ListAllInCorpus(dbConn, corpus);
				for(Document doc:docList) {
					corpus.documents.put(doc.getId(), doc);
				}
				corpus.activities = new HashMap<String, Activity>();
				List<Activity> activities = Activity.ListAllInCorpus(dbConn, corpus);
				for(Activity act:activities) {
					corpus.activities.put(act.getName(), act);
				}
				corpus.activityRoles = new HashMap<String, ActivityRole>();
				List<ActivityRole> activityRoles = ActivityRole.ListAllInCorpus(dbConn, corpus);
				for(ActivityRole actRole:activityRoles) {
					corpus.activityRoles.put(actRole.getName(), actRole);
				}
			}
			setNameMap(sc, myClass, nameMap);
			setIdMap(sc, myClass, idMap);
		}
		
		/**
		 * The documents in the corpus
		 */
	}
	
	private void clearMaps() {
		documents.clear();
		activities.clear();
		activityRoles.clear();
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
			"SELECT c.id, c.name, c.description, c.owner_id, count(*) nDocs, d.id docId"
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
									rs.getString("description"), rs.getInt("owner_id"), null);
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return corpus;
	}

	public static Corpus FindByName(Connection dbConn, String name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT c.id, c.name, c.description, c.owner_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id"
			+" WHERE c.name = ?"
			+" GROUP BY c.id";
		Corpus corpus = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
									rs.getString("description"), rs.getInt("owner_id"), null); 
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return corpus;
	}

	public static List<Corpus> ListAll(Connection dbConn) {
		// TODO Add pagination support
		final String SELECT_ALL = 
			"SELECT c.id, c.name, c.description, c.owner_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id"
			+" GROUP BY c.id";
		// Generate the right representation according to its media type.
		ArrayList<Corpus> corpusList = new ArrayList<Corpus>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Corpus corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
						rs.getString("description"), rs.getInt("owner_id"), null);
				// If no docId, count should actually be 0, not 1
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
				corpusList.add(corpus);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAll(): Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating corpus\n"+se.getLocalizedMessage()).build());
		}
		return corpusList;
	}	
	
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
			System.out.println(tmp);
	    	throw new WebApplicationException( 
	    			Response.status(
	    				Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
	}

	public static Corpus CreateAndPersist(Connection dbConn, 
			String name, String description, int owner_id, TimeSpan defaultDocTimeSpan) {
		final String myName = ".CreateAndPersist: ";
		final String INSERT_STMT = 
			"INSERT INTO corpus(name, description, owner_id, creation_time) VALUES(?,?,?,now())";
		Corpus corpus = null;
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
					corpus = new Corpus(rs.getInt(1), name, description, owner_id, defaultDocTimeSpan); 
				}
				rs.close();
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return corpus;
	}
	
	public void persist(Connection dbConn) {
		final String myName = ".persist: ";
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

	/**
	 * Deletes all current documents, and then rebuilds corpus from TEI
	 * @param docNode The TEI document to load this corpus from
	 * @param deepCreate If TRUE, will create documents as well.
	 * @param dbConn Connection for persistence
	 * @return count of documents created
	 * @throws XPathExpressionException
	 */
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
		if(documents != null) {
			nDocs = documents.size();
		} else {
			nDocs = fetchedDocumentCount;
		}
		return nDocs;
	}
	
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
			System.out.println(tmp);
		}
	}
	
	public void loadDocuments(Connection dbConn) {
		throw new RuntimeException("Not Yet Implemented");
	}

	public void addDocument( Document newDoc ) {
		//initMaps();
		documents.put(newDoc.getId(), newDoc);
	}
	
	public void deleteDocuments(Connection dbConn) {
		final String DELETE_DOCS = 
			"DELETE FROM document WHERE corpus_id = ?";
	    if(dbConn!=null) {
			try {
				PreparedStatement stmt = dbConn.prepareStatement(DELETE_DOCS);
				stmt.setInt(1, id);
				stmt.executeUpdate();
				stmt.close();
			} catch(SQLException se) {
				// Just absorb it
				String tmp = myClass+".deleteDocuments: Problem querying DB.\n"+ se.getMessage();
				System.out.println(tmp);
			}
	    }
		if(documents!=null)
			documents.clear();
	}

	public Activity findOrCreateActivity(String name) {
		throw new RuntimeException("NYI");
		/*
		initMaps();
		Activity instance = activities.get(name);
		if(instance == null) {
			instance = new Activity(name);
			activities.put(name, instance);
		}
		return instance;
		*/
	}

	public ActivityRole findOrCreateActivityRole(String name) {
		//initMaps();
		ActivityRole instance = activityRoles.get(name);
		if(instance == null) {
			instance = new ActivityRole(this, name);
			activityRoles.put(name, instance);
		}
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
				nameRoleActivitiesFilename, nameFamilyLinksFilename, documents);
    	System.out.println("Done.");
    	System.out.print("Generating Activities SQL...");
		SQLUtils.generateActivitiesSQL(activitiesFilename, activities);
    	System.out.println("Done.");
    	System.out.print("Generating ActivityRoles SQL...");
		SQLUtils.generateActivityRolesSQL(activityRolesFilename, activityRoles);
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
