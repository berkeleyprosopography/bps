package bps.services.corpus.main;

import java.util.ArrayList;

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
public class Document {
	private static int	nextID = 1;

	private int			id;				// Unique numeric id
	private String		alt_id;			// Secondary identifier string
	private String		sourceURL;		// TEI source for this document - may be relative
	private String		xml_id;			// Element within source (for compound files).
	private String		notes;			// Any notes on document
	private String		date_str;		// Date string from document
	private int			date_norm;		// Normalized date

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
	public Document(int id, String alt_id, String sourceURL, String xml_id,
			String notes, String date_str, int date_norm) {
		this.id = id;
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
	public Document(String alt_id, String sourceURL, String xml_id,
			String notes, String date_str, int date_norm) {
		this(Document.nextID++, alt_id, sourceURL, xml_id,
				notes, date_str, date_norm);
	}

	/**
	 * Create a new Document from an alt_id
	 * @param alt_id Secondary identifier string
	 */
	public Document(String alt_id) {
		this(Document.nextID++, alt_id, null, null, null, null, 0);
	}

	/**
	 * Create a new null Document.
	 */
	public Document() {
		this(Document.nextID++, null, null, null, null, null, 0);
	}

	/**
	 * @param teiNode The XML node for this Document
	 * @param deepCreate set to TRUE if this should also create Name, Activity, etc. instances
	 * @param corpus must be non-null if deepCreate is TRUE. All other instances are added to corpus lists.
	 * @return new Document
	 * @throws XPathExpressionException
	 */
	public static Document CreateFromTEI(Element teiNode, boolean deepCreate, Corpus corpus)
		throws XPathExpressionException {
		String alt_id = "unknown";
		//String notes = null;
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    Document newDoc = null;
	    try {
	        // XPath Query to get to the doc CDLI id
		    XPathExpression expr = xpath.compile("./teiHeader/fileDesc/titleStmt/title/name[@type='cdlicat:id_text']");
		    Element nameEl = (Element) expr.evaluate(teiNode, XPathConstants.NODE);
		    if(nameEl!=null)
		    	alt_id = nameEl.getTextContent().replaceAll("[\\s]+", " ");
		    newDoc = new Document(alt_id);
		    Activity unkActivity = corpus.findOrCreateActivity("Unknown");
		    ActivityRole principal = corpus.findOrCreateActivityRole("Principal");
		    ActivityRole witness = corpus.findOrCreateActivityRole("Witness");
		    if(deepCreate) {
		    	// Find the principal persName nodes and create a nameRoleActivity for each one
		    	newDoc.addNamesForActivity( ".//body//persName",
		    								teiNode, corpus, unkActivity, principal );
		    	// Find the witness persName nodes and create a nameRoleActivity for each one
		    	newDoc.addNamesForActivity( ".//back//div[@subtype='witnesses']//persName",
		    								teiNode, corpus, unkActivity, witness );
		    }
	    } catch (XPathExpressionException xpe) {
	    	// debug complaint
	    	throw xpe;
	    }
	    return newDoc;
	}

	public void addNamesForActivity( String xpathSel, Node context, Corpus corpus,
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
		        NodeList fnNodes = persNameEl.getElementsByTagName("forename");
			    int nNames = fnNodes.getLength();
		        if(nNames<1) {
		        	// Complain
		        } else {
		        	int patronymsLinked = 0;
				    for (int iN=0; iN < nNames; iN++) {
				        Element foreNameEl = (Element)fnNodes.item(iN);
				        String fnNameStr = foreNameEl.getAttribute("n").replaceAll("\\[.*\\]$", "");
				        if(fnNameStr.length()<1) {
				        	// Complain
				        } else {
				        	Name nameInstance = corpus.findOrCreateName(fnNameStr);
					        String fnXMLID = foreNameEl.getAttribute("xml:id");
					        if(fnXMLID.length()<1)
					        	fnXMLID = null;
					        String fnType = foreNameEl.getAttribute("type");
					        boolean isPatronym = fnType.equalsIgnoreCase("patronymic");
					        if(!isPatronym) {
					        	if(nra!=null) {
						        	// Complain - only one primary name
					        	} else {
						        	nra = new NameRoleActivity(nameInstance, defaultActRole,
						        			activity, fnXMLID);
						        	addNameRoleActivity(nra);
					        	}
					        } else if(nra==null) {
					        	// Complain - must already have primary name
					        } else {
					        	// Deal with patronyms - add a family link
					        	int linkType;
					        	if(patronymsLinked == 0) {
					        		linkType = NameFamilyLink.LINK_TO_FATHER;
					        	} else  if(patronymsLinked == 1) {
					        		linkType = NameFamilyLink.LINK_TO_GRANDFATHER;
					        	} else {
					        		linkType = NameFamilyLink.LINK_TO_ANCESTOR;
					        	}
					        	nra.addNameFamilyLink(nameInstance, linkType, fnXMLID);
					        	patronymsLinked++;
					        }
				        }
				    }
		        }
		        // Get the clan names
		        NodeList anNodes = persNameEl.getElementsByTagName("addName");
			    nNames = anNodes.getLength();
			    for (int iN=0; iN < nNames; iN++) {
			        Element addNameEl = (Element)anNodes.item(iN);
			        String anNameStr = addNameEl.getAttribute("n").replaceAll("\\[.*\\]$", "");
			        if(anNameStr.length()<1) {
			        	// Complain
			        } else {
			        	Name nameInstance = corpus.findOrCreateName(anNameStr);
				        String fnXMLID = addNameEl.getAttribute("xml:id");
				        if(fnXMLID.length()<1)
				        	fnXMLID = null;
				        if(!addNameEl.getAttribute("type").equalsIgnoreCase("clan")){
				        	// Complain - must be of type clan
				        } else if(nra==null) {
				        	// Complain - must already have primary name
				        } else {
				        	// add clan name
				        	nra.addNameFamilyLink(nameInstance, NameFamilyLink.LINK_TO_CLAN, fnXMLID);
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
	public int getDate_norm() {
		return date_norm;
	}

	/**
	 * @param date_norm the date_norm to set
	 */
	public void setDate_norm(int date_norm) {
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
		nameRoleActivities.add(new NameRoleActivity(name, role, activity, xmlID));
	}

	/**
	 * @return the nameRoleActivities
	 */
	public ArrayList<NameRoleActivity> getNameRoleActivities() {
		return nameRoleActivities;
	}

	/**
	 * @return alt_id.
	 */
	public String toString() {
		return "{"+((alt_id==null)?"(null)":alt_id)+"}";
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep) {
		return id+sep+'"'+alt_id+'"'+sep+'"'+sourceURL+'"'+sep+'"'+xml_id+'"'
				+sep+'"'+notes+'"'+sep+'"'+date_str+'"'+sep+date_norm;
	}

}
