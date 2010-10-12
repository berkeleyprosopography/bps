/**
 *
 */
package edu.berkeley.bps.services.corpus;
import edu.berkeley.bps.services.common.LinkTypes;

/**
 * @author pschmitz
 *
 */
public class NameFamilyLink {

	private static int	nextID = 1;

	private int					id;
	private Name				name;
	private LinkTypes			linkType;
	/**
	 * The ID of the token associated with this in the owning document
	 */
	private String				xmlID;

	/**
	 * Create a new empty instance.
	 */
	public NameFamilyLink() {
		this(nextID++, null, null, null);
	}

	/**
	 * @param nameRoleActivity the instance of a name being annotated
	 * @param name The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameFamilyLink(Name name, LinkTypes linkType, String xmlID) {
		this(nextID++, name, linkType, xmlID);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the instance to be created. Must be unique.
	 * @param name The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameFamilyLink(int id, Name name, LinkTypes linkType, String xmlID) {
		this.id = id;
		this.name = name;
		this.linkType = linkType;
		this.xmlID = xmlID;
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
	public Name getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * @return the linkType
	 */
	public LinkTypes getLinkType() {
		return linkType;
	}

	/**
	 * @param linkType the linkType to set
	 */
	public void setLinkType(LinkTypes linkType) {
		this.linkType = linkType;
	}

	/**
	 * @return the xmlID
	 */
	public String getXmlID() {
		return xmlID;
	}

	/**
	 * @param xmlID the xmlID to set
	 */
	public void setXmlID(String xmlID) {
		this.xmlID = xmlID;
	}

	public boolean isValid() {
		return !(name==null||linkType==null);
	}

	/**
	 * @return Name, link type, XML idref concatenated.
	 */
	public String toString() {
		return "{"+((name==null)?"?":name.toString())+","
				+linkType+","
				+((xmlID==null)?"?":xmlID)+"}";
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @param nullStr The null indicator to use for missing entries
	 * @param nraID id of the base NameRoleActivity instance
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(int nraID, String sep, String nullStr ) {
		if(!isValid())
			throw new RuntimeException(
			"Attempt to generate XML loadfile string for invalid NameFamilyLink.");
		return id+sep+
			nraID+sep+
			((name!=null)?name.getId():nullStr)+sep+
			linkType+sep+
			((xmlID!=null)?'"'+xmlID+'"':nullStr);
	}
}
