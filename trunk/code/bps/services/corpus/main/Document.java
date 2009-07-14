package bps.services.corpus.main;

import java.util.ArrayList;

import org.w3c.dom.Element;

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
	private ArrayList<NameFamilyLink> nameFamilyLinks;

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
		this.nameFamilyLinks = new ArrayList<NameFamilyLink>();
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
	 * Create a new null Document.
	 */
	public Document() {
		this(Document.nextID++, null, null, null, null, null, 0);
	}

	public static Document CreateFromTEI(Element docNode) {
		return new Document();
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

	public void addNameFamilyLink( NameFamilyLink nfl ) {
		nameFamilyLinks.add(nfl);
	}

	/**
	 * @param nameRoleActivity the instance of a name being annotated
	 * @param name The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public void addNameFamilyLink(NameRoleActivity nameRoleActivity, Name name,
			int linkType, String xmlID) {
		nameFamilyLinks.add(new NameFamilyLink(nameRoleActivity, name, linkType, xmlID));
	}

	/**
	 * @return the nameRoleActivities
	 */
	public ArrayList<NameRoleActivity> getNameRoleActivities() {
		return nameRoleActivities;
	}

	/**
	 * @return the nameFamilyLinks
	 */
	public ArrayList<NameFamilyLink> getNameFamilyLinks() {
		return nameFamilyLinks;
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
