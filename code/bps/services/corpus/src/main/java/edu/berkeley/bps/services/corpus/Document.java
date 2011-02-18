package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.LinkTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author pschmitz
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="document")
public class Document {
	private final static String myClass = "Document";

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
	@XmlElement
	private String		date_str;		// Date string from document
	private long		date_norm;		// Normalized date

	private ArrayList<NameRoleActivity> nameRoleActivities;

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
		this(CachedEntity.UNSET_ID_VALUE, corpus, alt_id, sourceURL, xml_id,
				notes, date_str, date_norm);
	}

	/**
	 * Create a new Document from an alt_id
	 * @param alt_id Secondary identifier string
	 */
	public Document(Corpus corpus, String alt_id, String date, long date_norm) {
		this(CachedEntity.UNSET_ID_VALUE, corpus, alt_id, null, null, null, date, date_norm);
	}

	/**
	 * Create a new null Document.
	 */
	public Document(Corpus corpus) {
		this(CachedEntity.UNSET_ID_VALUE, corpus, null, null, null, null, null, 0);
	}

	/**
	 * Create a new null Document.
	 */
	private Document() {
		this(CachedEntity.UNSET_ID_VALUE, null, null, null, null, null, null, 0);
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
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return document;
	}

	public static List<Document> ListAllInCorpus(Connection dbConn, Corpus corpus) {
		final String SELECT_BY_CORPUS_ID = 
			"SELECT id, alt_id, sourceURL, xml_id, notes, date_str"
			+" FROM document WHERE corpus_id = ?";
		int corpus_id = 0;
		if(corpus==null || (corpus_id=corpus.getId())<=0) {
			String tmp = myClass+".ListAllInCorpus: Invalid corpus.\n";
			System.out.println(tmp);
			throw new IllegalArgumentException( tmp );
		}
		ArrayList<Document> docList = new ArrayList<Document>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_CORPUS_ID);
			stmt.setInt(1, corpus_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Document document = new Document(rs.getInt("id"), corpus, rs.getString("alt_id"), 
						rs.getString("sourceURL"), rs.getString("xml_id"),
						rs.getString("notes"), rs.getString("date_str"), 0);
				docList.add(document);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllInCorpus: Problem querying DB.\n"+ se.getMessage();
			System.out.println(tmp);
			throw new RuntimeException( tmp );
		}
		return docList;
	}
	

	/**
	 * @param teiNode The XML node for this Document
	 * @param deepCreate set to TRUE if this should also create Name, Activity, etc. instances
	 * @param corpus must be non-null if deepCreate is TRUE. All other instances are added to corpus lists.
	 * @return new Document
	 * @throws XPathExpressionException
	 */
	public static Document CreateAndPersistFromTEI(Element teiNode, boolean deepCreate, Corpus corpus,
			Connection dbConn)
		throws XPathExpressionException {
		final String myName = ".CreateAndPersistFromTEI: ";
		final String INSERT_STMT = 
			"INSERT INTO document(corpus_id,alt_id,date_str,date_norm,creation_time)"
			+" VALUES(?,?,?,?,now())";
		String alt_id = "unknown";
		//String notes = null;
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    Document newDoc = null;
	    try {
	        // XPath Query to get to the doc CDLI id
		    XPathExpression expr = xpath.compile(TEI_Constants.XPATH_ALT_ID);
		    Element nameEl = (Element) expr.evaluate(teiNode, XPathConstants.NODE);
		    if(nameEl!=null)
		    	alt_id = nameEl.getTextContent().replaceAll("[\\s]+", " ");
		    // TODO Find the date, normalize, and pass it in.
		    // If no date, use the corpus midpoint date.
		    String date = null;
		    long date_norm = corpus.getDefaultDocTimeSpan().getCenterPoint();
		    try {
		    	PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
		    			Statement.RETURN_GENERATED_KEYS);
		    	stmt.setInt(1, corpus.getId());
		    	stmt.setString(2, alt_id);
		    	stmt.setString(3, date);
		    	stmt.setLong(3, date_norm);
		    	int nRows = stmt.executeUpdate();
		    	if(nRows==1){
		    		ResultSet rs = stmt.getGeneratedKeys();
		    		if(rs.next()){
		    			newDoc = new Document(rs.getInt(1), corpus, alt_id, 
		    					null, null, null, date, date_norm);
		    		}
		    		rs.close();
		    	}
		    } catch(SQLException se) {
		    	String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
		    	System.out.println(tmp);
		    	throw new RuntimeException( tmp );
		    }
		    
		    Activity unkActivity = corpus.findOrCreateActivity("Unknown");
		    // TODO - this is all corpus specific, and needs to go elsewhere!!!
		    ActivityRole principal = corpus.findOrCreateActivityRole("Principal");
		    ActivityRole witness = corpus.findOrCreateActivityRole("Witness");
		    List<String> missingNames = new ArrayList<String>(2);
		    missingNames.add("xxx");
		    missingNames.add("NUMMI");
		    // INCOMPLETE
				// List<Pattern> elides = new ArrayList<Pattern>(1);
		    // elides.add("[\\d*]");
		    
		    if(deepCreate) {
		    	// Find the principal persName nodes and create a nameRoleActivity for each one
		    	newDoc.addNamesForActivity( dbConn, TEI_Constants.XPATH_PRINCIPAL_PERSNAMES,
		    								teiNode, corpus, unkActivity, principal );
		    	// Find the witness persName nodes and create a nameRoleActivity for each one
		    	newDoc.addNamesForActivity( dbConn, TEI_Constants.XPATH_WITNESS_PERSNAMES,
		    								teiNode, corpus, unkActivity, witness );
		    }
	    } catch (XPathExpressionException xpe) {
	    	// debug complaint
	    	throw xpe;
	    }
	    return newDoc;
	}

	protected void addNamesForActivity( Connection dbConn, 
			String xpathSel, Node context, Corpus corpus,
			Activity activity, ActivityRole defaultActRole )
		throws XPathExpressionException {

	    XPath xpath = XPathFactory.newInstance().newXPath();
	    try {
			XPathExpression expr = xpath.compile(xpathSel);
		    NodeList nodes = (NodeList) expr.evaluate(context, XPathConstants.NODESET);
		    int nNodes = nodes.getLength();
		    for (int i=0; i < nNodes; i++) {
	        	NameRoleActivity nra = null;
		        Element persNameEl = (Element)nodes.item(i);
		        // Get the forenames
		        NodeList fnNodes = persNameEl.getElementsByTagName(TEI_Constants.FORENAME_EL);
			    int nNames = fnNodes.getLength();
		        if(nNames<1) {
		        	//generateParseError(persNameEl, TEI_Constants.PERSNAME_EL+" has no "
		        	//								+TEI_Constants.FORENAME_EL+" declarations.");
		        	// Create an empty NRAD to represent this
		        	nra = new NameRoleActivity(null, defaultActRole, activity, null, this);
		        	addNameRoleActivity(nra);
		        } else {
		        	int patronymsLinked = 0;
				    for (int iN=0; iN < nNames; iN++) {
				        Element foreNameEl = (Element)fnNodes.item(iN);
				        String fnNameStr = foreNameEl.getAttribute("n").replaceAll("\\[.*\\]$", "");
				        if(fnNameStr.length()<1) {
				        	generateParseError(foreNameEl,
				        			TEI_Constants.FORENAME_EL+" has no (or empty) value.");
				        	nra = new NameRoleActivity(null, defaultActRole, activity, null, this);
				        	addNameRoleActivity(nra);
				        } else {
					        String fnType = foreNameEl.getAttribute(TEI_Constants.TYPE_ATTR);
					        boolean isPatronym = fnType.equalsIgnoreCase(TEI_Constants.TYPE_PATRONYMIC);
				        	String nameGender = Name.GENDER_UNKNOWN;
					        if( isPatronym || fnType.equalsIgnoreCase(TEI_Constants.TYPE_GENDER_MASCULINE)) {
					        	nameGender = Name.GENDER_MALE;
					        } else if( fnType.equalsIgnoreCase(TEI_Constants.TYPE_GENDER_FEMININE)) {
					        	nameGender = Name.GENDER_FEMALE;
					        }
				        	Name nameInstance = Name.FindByName(dbConn, fnNameStr, corpus.getId());
				        	if(nameInstance != null ) {
				        		if(nameInstance.getGender()!=nameGender) {
						        	generateParseError(foreNameEl,
						        		"Name: "+fnNameStr+" gender does not match previous instance - ignoring.");
				        		}
				        	} else {
				        		nameInstance = Name.CreateAndPersist(dbConn, corpus.getId(),
				        				fnNameStr, Name.NAME_TYPE_PERSON, nameGender, null, null);
				        	}
					        String fnXMLID = foreNameEl.getAttribute(TEI_Constants.XMLID_ATTR);
					        if(fnXMLID.length()<1)
					        	fnXMLID = null;
					        if(!isPatronym) {
					        	if(nra!=null) {
						        	generateParseError(foreNameEl, "Multiple foreNames encountered.");
					        	} else {
						        	nra = new NameRoleActivity(nameInstance, defaultActRole,
						        			activity, fnXMLID, this);
						        	addNameRoleActivity(nra);
					        	}
					        } else{
					        	if(nra==null) {
						        	//generateParseError(foreNameEl, "Patronym with no primary forename.");
						        	// Create an empty NRAD to represent missing forename
						        	nra = new NameRoleActivity(null, defaultActRole, activity, null, this);
						        	addNameRoleActivity(nra);
					        	}
					        	// Deal with patronyms - add a family link
					        	LinkTypes linkType;
					        	if(patronymsLinked == 0) {
					        		linkType = LinkTypes.LINK_TO_FATHER;
					        	} else  if(patronymsLinked == 1) {
					        		linkType = LinkTypes.LINK_TO_GRANDFATHER;
					        	} else {
					        		linkType = LinkTypes.LINK_TO_ANCESTOR;
					        	}
					        	nra.addNameFamilyLink(nameInstance, linkType, fnXMLID);
					        	patronymsLinked++;
					        }
				        }
				    }
		        }
		        // Get the clan names
		        NodeList anNodes = persNameEl.getElementsByTagName(TEI_Constants.ADDNAME_EL);
			    nNames = anNodes.getLength();
			    boolean fFoundClanName = false;
			    for (int iN=0; iN < nNames; iN++) {
			        Element addNameEl = (Element)anNodes.item(iN);
			        String anNameStr = addNameEl.getAttribute("n").replaceAll("\\[.*\\]$", "");
			        if(anNameStr.length()<1) {
			        	generateParseError(addNameEl, "Additional name empty.");
			        } else {
			        	String nameGender = Name.GENDER_UNKNOWN;
				        String anTypeAttr = addNameEl.getAttribute(TEI_Constants.TYPE_ATTR);
				        boolean isClan = false;
				        boolean isSpouse = false;
				        String nametype = null;
				        if(anTypeAttr.equalsIgnoreCase(TEI_Constants.TYPE_CLAN)){
				        	isClan = true;
				        	nametype = Name.NAME_TYPE_CLAN;
				        	nameGender = Name.GENDER_MALE;
				        } else if(anTypeAttr.equalsIgnoreCase(TEI_Constants.TYPE_SPOUSE)){
				        	isSpouse = true;
				        	nametype = Name.NAME_TYPE_PERSON;
				        	Name baseName = nra.getName();
				        	if(baseName==null || baseName.getGender()==Name.GENDER_UNKNOWN) {
				        		nameGender = Name.GENDER_UNKNOWN;
				        	} else {
				        		nameGender = ( baseName.getGender()==Name.GENDER_MALE )?
				        				Name.GENDER_FEMALE:Name.GENDER_MALE;
				        	}
				        } else {
				        	generateParseError(addNameEl, "Additional name must be a clan or spouse.");
				        	continue;
				        }

			        	Name nameInstance = Name.FindByName(dbConn, anNameStr, corpus.getId());
			        	if(nameInstance != null ) {
			        		if(nameInstance.getGender()!=nameGender) {
					        	generateParseError(addNameEl,
					        		"Name: "+anNameStr+" gender does not match previous instance - ignoring.");
			        		}
			        		if(nameInstance.getNameType()!=nametype) {
					        	generateParseError(addNameEl,
					        		"Name: "+anNameStr+" nametype does not match previous instance - ignoring.");
			        		}
			        	} else {
			        		nameInstance = Name.CreateAndPersist(dbConn, corpus.getId(),
			        				anNameStr, nametype, nameGender, null, null);
			        	}
				        String fnXMLID = addNameEl.getAttribute(TEI_Constants.XMLID_ATTR);
				        if(fnXMLID.length()<1)
				        	fnXMLID = null;
				        if(nra==null) {
				        	generateParseError(addNameEl, "Additional name with no primary name.");
				        } else if(fFoundClanName) {
				        	generateParseError(addNameEl, "Multiple clan name declarations.");
				        } else if(isClan){
				        	// add clan name
				        	nra.addNameFamilyLink(nameInstance, LinkTypes.LINK_TO_CLAN, fnXMLID);
				        	fFoundClanName = true;
				        } else if(isSpouse){
				        	// add clan name
				        	nra.addNameFamilyLink(nameInstance, LinkTypes.LINK_TO_CLAN, fnXMLID);
				        	fFoundClanName = true;
				        }
			        }
			    }
		    }
	    } catch (XPathExpressionException xpe) {
	    	// debug complaint
	    	throw xpe;
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
		nameRoleActivities.add(new NameRoleActivity(name, role, activity, xmlID, this));
	}

	/**
	 * @return the nameRoleActivities
	 */
	public ArrayList<NameRoleActivity> getNameRoleActivities() {
		return nameRoleActivities;
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

	private void generateParseError( Element node, String errorStr ) {
		String nodeId = (node==null)?null:node.getAttribute(TEI_Constants.XMLID_ATTR);
		String fullString = "Parse error in Document "+toString();
		if(nodeId!=null) {
			fullString += " on element: "+nodeId;
		}
		System.err.println(fullString);
		System.err.println(errorStr);
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @param nullStr The null indicator to use for missing entries
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep, String nullStr ) {
		return id+sep+
			((corpus!=null)?corpus.getId():nullStr)+sep+
			((alt_id!=null)?'"'+alt_id+'"':nullStr)+sep+
			((sourceURL!=null)?'"'+sourceURL+'"':nullStr)+sep+
			((xml_id!=null)?'"'+xml_id+'"':nullStr)+sep+
			((notes!=null)?'"'+notes+'"':nullStr)+sep+
			((date_str!=null)?'"'+date_str+'"':nullStr)+sep+
			date_norm;
	}

}
