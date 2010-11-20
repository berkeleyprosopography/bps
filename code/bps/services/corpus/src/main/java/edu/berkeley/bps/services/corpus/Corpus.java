package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.time.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Corpus {
	private final static String myClass = "Corpus";
	private static int	nextID = 1;

	private int			id;
	private String		name;
	private String		description;
	private TimeSpan	defaultDocTimeSpan = null;
	// TODO Link to a User instance from bps.services.user.main
	//private User		owner;

	/**
	 * The documents in the corpus
	 */
	private HashMap<Integer, Document> documents;
	/**
	 * The named activities (not instances) seen in this corpus
	 */
	private HashMap<String, Activity> activities;
	/**
	 * The named roles in activities (not instances) for this corpus
	 */
	private HashMap<String, ActivityRole> activityRoles;
	/**
	 * The Names seen in this corpus (not instances)
	 */
	private HashMap<String, Name> names;

	/**
	 * Create a new empty corpus.
	 */
	public Corpus() {
		this(Corpus.nextID++, null, null, null);
	}

	/**
	 * Create a new corpus with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus( String name, String description, TimeSpan defaultDocTimeSpan ) {
		this(Corpus.nextID++, name, description, defaultDocTimeSpan);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see Corpus( String name, String description )
	 * @param id ID of the corpus to be created. Must be unique.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus(int id, String name, String description, TimeSpan defaultDocTimeSpan) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.defaultDocTimeSpan = defaultDocTimeSpan;
		documents = new HashMap<Integer, Document>();
		activities = new HashMap<String, Activity>();
		activityRoles = new HashMap<String, ActivityRole>();
		names = new HashMap<String, Name>();
	}
	
	public static Corpus FindByID(Connection dbConn, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = "SELECT id, name, description FROM corpus WHERE id = ?";
		Corpus corpus = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
									rs.getString("description"), null); 
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
		final String SELECT_BY_NAME = "SELECT id, name, description FROM corpus WHERE name = ?";
		Corpus corpus = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				if(rs.next()){
					corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
										rs.getString("description"), null); 
				}
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
					corpus = new Corpus(rs.getInt(1), name, description, defaultDocTimeSpan); 
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
		final String myName = ".CreateAndPersist: ";
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
	
	public static Corpus CreateFromTEI(org.w3c.dom.Document docNode, boolean deepCreate,
			TimeSpan defaultDocTimeSpan)
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
					    Document document = Document.CreateFromTEI(teiEl, true, newCorpus);
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addDocument( Document newDoc ) {
		documents.put(newDoc.getId(), newDoc);
	}

	public Activity findOrCreateActivity(String name) {
		Activity instance = activities.get(name);
		if(instance == null) {
			instance = new Activity(name);
			activities.put(name, instance);
		}
		return instance;
	}

	public Name findOrCreateName(String name) {
		Name instance = names.get(name);
		if(instance == null) {
			instance = new Name(name);
			names.put(name, instance);
		}
		return instance;
	}

	public ActivityRole findOrCreateActivityRole(String name) {
		ActivityRole instance = activityRoles.get(name);
		if(instance == null) {
			instance = new ActivityRole(name);
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
    	System.out.print("Generating Names SQL...");
		SQLUtils.generateNamesSQL(namesFilename, names);
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
