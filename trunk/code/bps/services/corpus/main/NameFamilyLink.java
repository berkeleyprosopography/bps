/**
 *
 */
package bps.services.corpus.main;

/**
 * @author pschmitz
 *
 */
public class NameFamilyLink {
	public static final int LINK_TO_UNDEFINED = -1;
	private static final int LINK_TYPE_MIN = 0;
	public static final int LINK_TO_FATHER = 0;
	public static final int LINK_TO_MOTHER = 1;
	public static final int LINK_TO_GRANDFATHER = 2;
	public static final int LINK_TO_GRANDMOTHER = 3;
	public static final int LINK_TO_ANCESTOR = 4;
	public static final int LINK_TO_CLAN = 5;
	private static final int LINK_TYPE_MAX = 5;

	private static int	nextID = 1;

	private int					id;
	private Name				name;
	private int					linkType;
	/**
	 * The ID of the token associated with this in the owning document
	 */
	private String				xmlID;

	/**
	 * Create a new empty instance.
	 */
	public NameFamilyLink() {
		this(nextID++, null, LINK_TO_UNDEFINED, null);
	}

	/**
	 * @param nameRoleActivity the instance of a name being annotated
	 * @param name The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameFamilyLink(Name name, int linkType, String xmlID) {
		this(nextID++, name, linkType, xmlID);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @param id ID of the instance to be created. Must be unique.
	 * @param name The Name of the linked family member (or clan)
	 * @param linkType one of the LINK_TO_* constants defined in the class
	 * @param xmlID The ID of the token associated with this in the owning document
	 */
	public NameFamilyLink(int id, Name name, int linkType, String xmlID) {
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
	public int getLinkType() {
		return linkType;
	}

	/**
	 * @param linkType the linkType to set
	 */
	public void setLinkType(int linkType) {
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
		return !(name==null
				||linkType<LINK_TYPE_MIN||linkType>LINK_TYPE_MAX);
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
	 * @param nraID id of the base NameRoleActivity instance
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep, int nraID) {
		if(!isValid())
			throw new RuntimeException(
					"Attempt to generate XML loadfile string for invalid NameFamilyLink.");
		return id+sep+nraID+sep+name.getId()+sep+linkType
				+sep+((xmlID==null)?"\\N":xmlID);
	}
}
